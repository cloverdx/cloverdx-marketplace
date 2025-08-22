package com.cloverdx.libraries.gcloud;

import com.cloverdx.libraries.gcloud.BigQueryBulkWriter.BigQueryWriter;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.bigquery.JobInfo.WriteDisposition;
import com.google.cloud.bigquery.storage.v1.AppendRowsResponse;
import com.google.cloud.bigquery.storage.v1.BigQueryWriteClient;
import com.google.cloud.bigquery.storage.v1.BigQueryWriteSettings;
import com.google.cloud.bigquery.storage.v1.CreateWriteStreamRequest;
import com.google.cloud.bigquery.storage.v1.Exceptions;
import com.google.cloud.bigquery.storage.v1.FinalizeWriteStreamResponse;
import com.google.cloud.bigquery.storage.v1.JsonStreamWriter;
import com.google.cloud.bigquery.storage.v1.TableName;
import com.google.cloud.bigquery.storage.v1.WriteStream;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import io.grpc.Status;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.concurrent.GuardedBy;
import org.jetel.exception.JetelRuntimeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataWriterCommitted extends CloverBigQueryWriter {
	private int maxRetryCount = 3;
	private int maxRecreateCount = 3;
	private static final ImmutableList<Status.Code> RETRIABLE_ERROR_CODES = ImmutableList.of(Status.Code.INTERNAL,
			Status.Code.ABORTED, Status.Code.CANCELLED, Status.Code.FAILED_PRECONDITION, Status.Code.DEADLINE_EXCEEDED,
			Status.Code.UNAVAILABLE);

	private JsonStreamWriter streamWriter;

	// Track the number of in-flight requests to wait for all responses before shutting down.
	private final Phaser inflightRequestCount = new Phaser(1);

	private final Object lock = new Object();
	
	private GoogleCredentials credentials;

	private BigQueryWriteSettings bigQueryWriteSettings;

	private BigQueryWriter cloverParent;

	private boolean ignoreUnknownFields = true;

	private boolean propagateErrorRecord = true;

	private AtomicInteger recreateCount = new AtomicInteger(0);

	private WriteStream writeStream;

	private BigQueryWriteClient client;
	
	private TableName parentTable;

	@GuardedBy("lock")
	private RuntimeException error = null;
	
	WriteStream.Type writerType = null;

	public void initialize(TableName parentTable, BigQueryWriteClient client, String credentialsPath, String type, boolean ignoreUnknownFields, int maxRetryCount, int maxRecreateCount) throws Descriptors.DescriptorValidationException, IOException, InterruptedException {
		if (credentialsPath != null && !credentialsPath.isEmpty()) {
			try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
				this.credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
			}
		}
		
		if(type.equals("committed")) {
			writerType = WriteStream.Type.COMMITTED;
		}
		else if(type.equals("pending")) {
			writerType = WriteStream.Type.PENDING;
			
		}
		else {
			throw new JetelRuntimeException("Unsuppoerted write stream type "+type);
		}
		this.maxRetryCount = maxRetryCount;
		this.maxRecreateCount = maxRecreateCount;
		this.ignoreUnknownFields = ignoreUnknownFields;
		this.initialize(parentTable, client, writerType);
	}

	public void initialize(TableName parentTable, BigQueryWriteClient client, WriteStream.Type type)
			throws IOException, DescriptorValidationException, InterruptedException {
		
		this.client = client;
		this.parentTable = parentTable;
		
		if (this.getCredentials() != null) {
			bigQueryWriteSettings = BigQueryWriteSettings.newBuilder()
					.setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials())).build();
		} else {
			bigQueryWriteSettings = BigQueryWriteSettings.newBuilder().build();
		}

		// client = BigQueryWriteClient.create(this.getBigQueryWriteSettings())
		WriteStream stream = WriteStream.newBuilder().setType(type).build();

		CreateWriteStreamRequest createWriteStreamRequest = CreateWriteStreamRequest.newBuilder()
				.setParent(parentTable.toString()).setWriteStream(stream).build();
		this.writeStream = client.createWriteStream(createWriteStreamRequest);

		// Use the JSON stream writer to send records in JSON format.
		// For more information about JsonStreamWriter, see:
		// https://googleapis.dev/java/google-cloud-bigquerystorage/latest/com/google/cloud/bigquery/storage/v1/JsonStreamWriter.html
		streamWriter = JsonStreamWriter.newBuilder(writeStream.getName(), this.writeStream.getTableSchema(), client)
				.setIgnoreUnknownFields(ignoreUnknownFields).build();
	}

	public void append(AppendContext context, long offset)
			throws DescriptorValidationException, IOException, ExecutionException {

		synchronized (this.lock) {
			if (!streamWriter.isUserClosed() && streamWriter.isClosed()
					&& recreateCount.getAndIncrement() < maxRecreateCount) {
				try {
					streamWriter = JsonStreamWriter
							.newBuilder(writeStream.getName(), this.writeStream.getTableSchema(), this.client)
							.setIgnoreUnknownFields(ignoreUnknownFields).build();
					this.error = null;
				} catch (IllegalArgumentException | DescriptorValidationException | IOException
						| InterruptedException e) {
					System.out.println("Unable to recreate connection.");
				}
			}
			// If earlier appends have failed, we need to reset before continuing.
			if (this.error != null) {
				throw this.error;
			}
		}

		// Append asynchronously for increased throughput.
		ApiFuture<AppendRowsResponse> future = streamWriter.append(context.data, offset);
		ApiFutures.addCallback(future, new DataWriterCommitted.AppendCompleteCallback(this, context),
				MoreExecutors.directExecutor());
		// Increase the count of in-flight requests.
		inflightRequestCount.register();
	}

	public void cleanup(BigQueryWriteClient client) {
		// Wait for all in-flight requests to complete.
		inflightRequestCount.arriveAndAwaitAdvance();

		// Close the connection to the server.
		streamWriter.close();

		// Verify that no error occurred in the stream.
		synchronized (this.lock) {
			if (this.error != null) {
				throw this.error;
			}
		}

		// Finalize the stream.
		FinalizeWriteStreamResponse finalizeResponse = client.finalizeWriteStream(streamWriter.getStreamName());
		System.out.println("Rows written: " + finalizeResponse.getRowCount());

		
	}

	
	public int getNumRows() throws JobException, InterruptedException{
	    String queryRowCount =
	        "SELECT COUNT(*) FROM `"
	            + this.parentTable.getProject()
	            + "."
	            + this.parentTable.getDataset()
	            + "."
	            + this.parentTable.getTable()
	            + "`";
	    
	    QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(queryRowCount).build();
	    BigQuery bigquery = BigQueryOptions.newBuilder().setCredentials(this.credentials).build().getService();
	    TableResult results = bigquery.query(queryConfig);
	    int countRowsActual = Integer.parseInt(results.getValues().iterator().next().get("f0_").getStringValue());
	    
	    return countRowsActual;
	}
	
	public long truncate() throws JobException, InterruptedException{
	    String queryRowCount =
	        "TRUNCATE TABLE `"
	            + this.parentTable.getProject()
	            + "."
	            + this.parentTable.getDataset()
	            + "."
	            + this.parentTable.getTable()
	            + "`";
	    
	    QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(queryRowCount).build();
	    BigQuery bigquery = BigQueryOptions.newBuilder().setCredentials(this.credentials).build().getService();
	    TableResult results = bigquery.query(queryConfig);
	    return results.getTotalRows();
	}
	
	public String getStreamName() {
		return streamWriter.getStreamName();
	}

	public void setErrorHandlerClass(BigQueryWriter parent) {
		this.cloverParent = parent;
	}

	public GoogleCredentials getCredentials() {
		return credentials;
	}
	
	public BigQueryWriteSettings getBigQueryWriteSettings() {
		return bigQueryWriteSettings;
	}
	
	static class AppendCompleteCallback implements ApiFutureCallback<AppendRowsResponse> {

		private final DataWriterCommitted parent;
		private final AppendContext appendContext;

		public AppendCompleteCallback(DataWriterCommitted parent, AppendContext appendContext) {
			this.parent = parent;
			this.appendContext = appendContext;
		}

		public void onSuccess(AppendRowsResponse response) {
			this.parent.recreateCount.set(0);
			//System.out.format("Append %d success\n", response.getAppendResult().getOffset().getValue());
			done();
		}

		public void onFailure(Throwable throwable) {
			// If the wrapped exception is a StatusRuntimeException, check the state of the operation.
			// If the state is INTERNAL, CANCELLED, or ABORTED, you can retry. For more information,
			// see: https://grpc.github.io/grpc-java/javadoc/io/grpc/StatusRuntimeException.html
			Status status = Status.fromThrowable(throwable);
			if (appendContext.retryCount < this.parent.maxRetryCount && RETRIABLE_ERROR_CODES.contains(status.getCode())) {
				appendContext.retryCount++;
				try {
					// Since default stream appends are not ordered, we can simply retry the appends.
					// Retrying with exclusive streams requires more careful consideration.
					this.parent.append(appendContext);
					// Mark the existing attempt as done since it's being retried.
					done();
					return;
				} catch (Exception e) {
					System.out.format("Failed to retry append: %s\n", e);
					throw new JetelRuntimeException(e);
				}
			}

			if (throwable instanceof Exceptions.AppendSerializationError) {
				Exceptions.AppendSerializationError ase = (Exceptions.AppendSerializationError) throwable;
				Map<Integer, String> rowIndexToErrorMessage = ase.getRowIndexToErrorMessage();
				if (rowIndexToErrorMessage.size() > 0) {
					// Omit the faulty rows
					JSONArray dataNew = new JSONArray();
					for (int i = 0; i < appendContext.data.length(); i++) {
						if (!rowIndexToErrorMessage.containsKey(i)) {
							try {
								dataNew.put(appendContext.data.get(i));
							} catch (JSONException e) {
								throw new JetelRuntimeException(e);
							}
						} else {
							// process faulty rows
							if(this.parent.propagateErrorRecord) {
								try {
									JSONObject wrongRecord = (JSONObject) appendContext.metadata.get(i);
									this.parent.cloverParent.processError(wrongRecord,
											"Unable to upload row number " + wrongRecord.get("_cloverRowNum") + ". Reason: "
													+ rowIndexToErrorMessage.get(i) + ". Record details: "
													+ appendContext.data.get(i).toString(),
											(int) wrongRecord.get("_cloverRowNum"));
								} catch (JSONException e) {
									System.out.println("Unable to serialize wrong record.");
								}
							}
						}
					}

					// Retry the remaining valid rows, but using a separate thread to
					// avoid potentially blocking while we are in a callback.
					if (dataNew.length() > 0) {
						try {
							this.parent.append(new AppendContext(dataNew, 0));
						} catch (DescriptorValidationException | IOException | InterruptedException e) {
							throw new RuntimeException(e);
						}
                    }
					// Mark the existing attempt as done since we got a response for it
					done();
					return;
				}
			}

			synchronized (this.parent.lock) {
				if (this.parent.error == null) {
					Exceptions.StorageException storageException = Exceptions.toStorageException(throwable);
					this.parent.error = (storageException != null) ? storageException : new RuntimeException(throwable);
				}
			}
			done();
		}

		private void done() {
			// Reduce the count of in-flight requests.
			this.parent.inflightRequestCount.arriveAndDeregister();
		}
	}
}