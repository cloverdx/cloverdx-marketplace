package com.cloverdx.libraries.gcloud;

import com.cloverdx.libraries.gcloud.BigQueryBulkWriter.BigQueryWriter;
import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.core.FixedExecutorProvider;
import com.google.api.gax.grpc.ChannelPoolSettings;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.JobInfo.CreateDisposition;
import com.google.cloud.bigquery.JobInfo.WriteDisposition;
import com.google.cloud.bigquery.QueryJobConfiguration;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.bigquery.TableResult;
import com.google.cloud.bigquery.storage.v1.*;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Descriptors;
import io.grpc.Status;
import org.jetel.exception.JetelRuntimeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.concurrent.GuardedBy;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class DataWriterDefaultStream extends CloverBigQueryWriter {

	private int maxRetryCount = 3;
	private int maxRecreateCount = 3;
	private static final ImmutableList<Status.Code> RETRIABLE_ERROR_CODES = ImmutableList.of(Status.Code.INTERNAL,
			Status.Code.ABORTED, Status.Code.CANCELLED, Status.Code.FAILED_PRECONDITION, Status.Code.DEADLINE_EXCEEDED,
			Status.Code.UNAVAILABLE);

	// Track the number of in-flight requests to wait for all responses before
	// shutting down.
	private final Phaser inflightRequestCount = new Phaser(1);
	private final Object lock = new Object();
	private JsonStreamWriter streamWriter;

	public GoogleCredentials getCredentials() {
		return credentials;
	}

	private GoogleCredentials credentials;

	public BigQueryWriteSettings getBigQueryWriteSettings() {
		return bigQueryWriteSettings;
	}

	private boolean ignoreUnknownFields = true;

	private boolean propagateErrorRecord = true;

	private ChannelPoolSettings channelPoolSettings;

	private BigQueryWriteSettings bigQueryWriteSettings;

	private BigQueryWriter cloverParent;

	private TableName parentTable;
	
	private BigQueryWriteClient client;

	@GuardedBy("lock")
	private RuntimeException error = null;

	private AtomicInteger recreateCount = new AtomicInteger(0);

	public void initialize(TableName parentTable, String credentialsPath, boolean ignoreUnknownFields, int maxRetryCount, int maxRecreateCount) throws Descriptors.DescriptorValidationException, IOException, InterruptedException {
		if (credentialsPath != null && !credentialsPath.isEmpty()) {
			try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
				this.credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
			}
		}
		this.maxRetryCount = maxRetryCount;
		this.maxRecreateCount = maxRecreateCount;
		this.ignoreUnknownFields = ignoreUnknownFields;
		this.initialize(parentTable);
	}

	public void initialize(TableName parentTable)
			throws Descriptors.DescriptorValidationException, IOException, InterruptedException {
		
		this.parentTable = parentTable;
		if (this.getCredentials() != null) {
			bigQueryWriteSettings = BigQueryWriteSettings.newBuilder()
					.setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials())).build();
		} else {
			bigQueryWriteSettings = BigQueryWriteSettings.newBuilder().build();
		}
		
		ChannelPoolSettings.Builder channelPoolSettingsBuilder = ChannelPoolSettings.builder();
		this.channelPoolSettings = channelPoolSettingsBuilder.setInitialChannelCount(2).build();
		client = BigQueryWriteClient.create(this.getBigQueryWriteSettings());
		
		streamWriter = JsonStreamWriter
				.newBuilder(parentTable.toString(), client)
				.setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials()))
				.setIgnoreUnknownFields(ignoreUnknownFields)
				.setExecutorProvider(FixedExecutorProvider.create(Executors.newScheduledThreadPool(100)))
				.setChannelProvider(BigQueryWriteSettings.defaultGrpcTransportProviderBuilder()
						.setKeepAliveTime(org.threeten.bp.Duration.ofMinutes(1))
						.setKeepAliveTimeout(org.threeten.bp.Duration.ofMinutes(1)).setKeepAliveWithoutCalls(true)
						.setChannelPoolSettings(channelPoolSettings).build())
				.setEnableConnectionPool(true).build();

	}

	public void append(AppendContext appendContext)
			throws Descriptors.DescriptorValidationException, IOException, InterruptedException {
		synchronized (this.lock) {
			if (!streamWriter.isUserClosed() && streamWriter.isClosed()
					&& recreateCount.getAndIncrement() < this.maxRecreateCount) {
				streamWriter = JsonStreamWriter
						.newBuilder(this.parentTable.toString(),
								BigQueryWriteClient.create(this.getBigQueryWriteSettings()))
						.setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials()))
						.setIgnoreUnknownFields(ignoreUnknownFields)
						.setExecutorProvider(FixedExecutorProvider.create(Executors.newScheduledThreadPool(100)))
						.setChannelProvider(BigQueryWriteSettings.defaultGrpcTransportProviderBuilder()
								.setKeepAliveTime(org.threeten.bp.Duration.ofMinutes(1))
								.setKeepAliveTimeout(org.threeten.bp.Duration.ofMinutes(1))
								.setKeepAliveWithoutCalls(true).setChannelPoolSettings(this.channelPoolSettings)
								.build())
						.setEnableConnectionPool(true).build();
				this.error = null;
			}
			// If earlier appends have failed, we need to reset before continuing.
			if (this.error != null) {
				throw this.error;
			}
		}

		// Append asynchronously for increased throughput.
		ApiFuture<AppendRowsResponse> future = streamWriter.append(appendContext.data);
		ApiFutures.addCallback(future, new DataWriterDefaultStream.AppendCompleteCallback(this, appendContext),
				MoreExecutors.directExecutor());

		// Increase the count of in-flight requests.
		inflightRequestCount.register();
	}

	public void cleanup() {
		try {
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
			
			client.shutdown();
        

            if (!client.awaitTermination(10, TimeUnit.SECONDS)) {
                client.shutdownNow();
            }                   
        } catch (Exception e) {              
            client.shutdownNow();
            e.printStackTrace();
        }
	}

	public void setErrorHandlerClass(BigQueryWriter parent) {
		this.cloverParent = parent;
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
	    
	    QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(queryRowCount).setCreateDisposition(CreateDisposition.CREATE_IF_NEEDED).setWriteDisposition(WriteDisposition.WRITE_TRUNCATE).build();
	    BigQuery bigquery = BigQueryOptions.newBuilder().setCredentials(this.credentials).build().getService();
	    TableResult results = bigquery.query(queryConfig);
	    int countRowsActual = Integer.parseInt(results.getValues().iterator().next().get("f0_").getStringValue());
	    
	    return countRowsActual;
	}
	
	public long truncate() throws JobException, InterruptedException{
	    String queryTruncate =
	        "TRUNCATE TABLE `"
	            + this.parentTable.getProject()
	            + "."
	            + this.parentTable.getDataset()
	            + "."
	            + this.parentTable.getTable()
	            + "`";
	    
	    QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(queryTruncate).build();
	    BigQuery bigquery = BigQueryOptions.newBuilder().setCredentials(this.credentials).build().getService();
	    TableResult results = bigquery.query(queryConfig);
	    return results.getTotalRows();
	}
	
	public boolean createTable(String datasetName, String tableName, Schema schema) {
		boolean ret = false;
		try {
		    BigQuery bigquery = BigQueryOptions.newBuilder().setCredentials(this.credentials).build().getService();
	
		    TableId tableId = TableId.of(datasetName, tableName);
		    TableDefinition tableDefinition = StandardTableDefinition.of(schema);
		    TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();

		    Table table = bigquery.create(tableInfo);
			ret= table.exists();
		} catch (BigQueryException e) {
		      System.out.println("Table was not created. \n" + e.toString());
		      ret = false;
		}
		return ret;
	}

	static class AppendCompleteCallback implements ApiFutureCallback<AppendRowsResponse> {

		private final DataWriterDefaultStream parent;
		private final AppendContext appendContext;

		public AppendCompleteCallback(DataWriterDefaultStream parent, AppendContext appendContext) {
			this.parent = parent;
			this.appendContext = appendContext;
		}

		public void onSuccess(AppendRowsResponse response) {

			// TODO: propagate table schema if changed
			//TableSchema schema = response.getUpdatedSchema();
			// System.out.format("Append success\n");
			this.parent.recreateCount.set(0);
			done();
		}

		public void onFailure(Throwable throwable) {
			// If the wrapped exception is a StatusRuntimeException, check the state of the
			// operation.
			// If the state is INTERNAL, CANCELLED, or ABORTED, you can retry. For more
			// information,
			// see:
			// https://grpc.github.io/grpc-java/javadoc/io/grpc/StatusRuntimeException.html
			Status status = Status.fromThrowable(throwable);
			if (appendContext.retryCount < parent.maxRetryCount && RETRIABLE_ERROR_CODES.contains(status.getCode())) {
				appendContext.retryCount++;
				try {
					// Since default stream appends are not ordered, we can simply retry the
					// appends.
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
				if (!rowIndexToErrorMessage.isEmpty()) {
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
							if (this.parent.propagateErrorRecord) {
								try {
									JSONObject wrongRecord = (JSONObject) appendContext.metadata.get(i);
									this.parent.cloverParent.processError(wrongRecord,
											"Unable to upload row number " + wrongRecord.get("_cloverRowNum")
													+ ". Reason: " + rowIndexToErrorMessage.get(i)
													+ ". Record details: " + appendContext.data.get(i).toString(),
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
						} catch (Descriptors.DescriptorValidationException | IOException | InterruptedException e) {
							throw new JetelRuntimeException(e.getMessage());
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
	
	public String getStreamName() {
		return streamWriter.getStreamName();
	}
}
