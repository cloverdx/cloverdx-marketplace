package com.cloverdx.libraries.gcloud.BigQueryBulkWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataField;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.ConfigurationStatus.Priority;
import org.jetel.exception.ConfigurationStatus.Severity;
import org.jetel.exception.JetelRuntimeException;
import org.jetel.metadata.DataRecordMetadata;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.cloud.bigquery.storage.v1.Exceptions.AppendSerializationError;
import com.google.cloud.bigquery.storage.v1.StorageError;
import com.google.api.gax.paging.Page;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQuery.TableListOption;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.DatasetId;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableDefinition;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.cloud.bigquery.storage.v1.BatchCommitWriteStreamsRequest;
import com.google.cloud.bigquery.storage.v1.BigQueryWriteClient;
import com.google.cloud.bigquery.storage.v1.BigQueryWriteSettings;
import com.google.cloud.bigquery.storage.v1.TableName;
import com.google.cloud.bigquery.storage.v1.BatchCommitWriteStreamsResponse;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.cloverdx.libraries.gcloud.AppendContext;
import com.cloverdx.libraries.gcloud.CloverBigQueryWriter;
import com.cloverdx.libraries.gcloud.DataWriterCommitted;
import com.cloverdx.libraries.gcloud.DataWriterDefaultStream;
import com.cloverdx.libraries.gcloud.GoogleCloudUtils;
import org.jetel.metadata.DataFieldMetadata;

public class BigQueryWriter extends AbstractGenericTransform {

	private boolean isErrorPortConnected = false;
	private boolean hasErrorPortAssignedMetadata = false;
	private String projectId;
	private String datasetName;
	private String tableName;
	private String pathToCredentials ;
	private int batchSize ;
	private boolean enableRejectInformation = true;
	private boolean ignoreUnknownFields = true;
	private String processingType = "defaultStream";
	private int maxRetryCount = 3;
	private int maxRecreateCount = 3;
	private boolean truncateTable = true;
	private String writeMode = "APPEND"; 
	private GoogleCredentials credentials;
	 
	private BigQueryWriteSettings bigQueryWriteSettings;

	private CloverBigQueryWriter writer;
	
	@Override
	public void execute() {
		
		/* Load configuration properties */
		projectId = 				getProperties().getStringProperty("PROJECT_ID");
		datasetName = 				getProperties().getStringProperty("DATASET_NAME");
		tableName = 				getProperties().getStringProperty("TABLE_NAME");
		pathToCredentials = 		getProperties().getStringProperty("PATH_TO_CREDENTIALS");
		batchSize = 				getProperties().getIntProperty("BATCH_SIZE");
		enableRejectInformation = 	getProperties().getBooleanProperty("ENABLE_REJECT_INFORMATION");
		ignoreUnknownFields = 		getProperties().getBooleanProperty("IGNORE_UNKWNOWN_FIELDS");
		processingType = 			getProperties().getStringProperty("PROCESSING_TYPE");
		maxRetryCount = 			getProperties().getIntProperty("MAX_RETRY_COUNT");
		maxRecreateCount = 			getProperties().getIntProperty("MAX_RECREATE_COUNT");
		writeMode = 				getProperties().getStringProperty("WRITE_MODE");

		if(writeMode!=null) {
            truncateTable = writeMode.equals("TRUNCATE");
		}
		else {
			writeMode = "APPEND";
			truncateTable = false;
		}
			
		// batch counter 
		int i = 0;
		
		// total record counter
		int recCounter = 0;
		
		// offset counter
		long offset = 0;


		// Array containing data for append batch
		JSONArray jsonArr = new JSONArray();

		// Copy of the data, used for storing extra information for error records
        JSONArray jsonArrMeta = new JSONArray();
        
		// Setting the target table
        TableName parentTable = TableName.of(projectId, datasetName, tableName);
        
        // Used in committed mode
        BigQueryWriteClient client = null;
        
        getLogger().log(Level.INFO, "Processing type is: "+processingType);

        initCredentials();
        
        createTableIfNeeded();

        if(processingType.equals("defaultStream")) {
        	writer = new DataWriterDefaultStream();
        	try {

        		getLogger().log(Level.INFO, "Initializing Default Stream Writer");
        		parentTable = TableName.of(projectId, datasetName, tableName);
    			writer.initialize(parentTable, pathToCredentials, ignoreUnknownFields, maxRetryCount, maxRecreateCount);
    			// reference to this class - used for an error handling
    			writer.setErrorHandlerClass(this);
    			
    			if(truncate()) {
    				writer.cleanup();
    				getLogger().log(Level.INFO, "Re-Initializing Default Stream Writer after truncating the table");
        			writer.initialize(parentTable, pathToCredentials, ignoreUnknownFields, maxRetryCount, maxRecreateCount);
        			// reference to this class - used for an error handling
        			writer.setErrorHandlerClass(this);
    			}
        	} catch (Exception e) {
    			writer.cleanup();
    			throw new JetelRuntimeException("Unable to initialize DataWriterDefaultStream. "+ e.getMessage());
    		}
        	
        } else if(processingType.equals("committed") || processingType.equals("pending")) {
            getLogger().log(Level.INFO, "Initializing "+processingType+" Writer");
        	writer = new DataWriterCommitted();        	        	
        	
        	try {
    			client = BigQueryWriteClient.create(this.getBigQueryWriteSettings());
        		parentTable = TableName.of(projectId, datasetName, tableName);
    			writer.initialize(parentTable, client, pathToCredentials, processingType, ignoreUnknownFields, maxRetryCount, maxRecreateCount);
    			// reference to this class - used for an error handling
    			writer.setErrorHandlerClass(this);
    			
    			if(truncate()) {
    				writer.cleanup(client);
    				try {
                        client.shutdown();
            			if (!client.awaitTermination(10, TimeUnit.SECONDS)) {
            				client.shutdownNow();
            			}
            		} catch (Exception e) {
            			client.shutdownNow();
            		}
    				
    				// re-initialize client and writer
    				getLogger().log(Level.INFO, "Re-Initializing writer after truncating the table");
    				client = BigQueryWriteClient.create(this.getBigQueryWriteSettings());
        			writer.initialize(parentTable, client, pathToCredentials, processingType, ignoreUnknownFields, maxRetryCount, maxRecreateCount);
        			// reference to this class - used for an error handling
        			writer.setErrorHandlerClass(this);
    			}
    			
        	} catch (Exception e) {
    			writer.cleanup(client);
    			try {
                    client.shutdown();
        			if (!client.awaitTermination(10, TimeUnit.SECONDS)) {
        				client.shutdownNow();
        			}
        		} catch (Exception e2) {
        			client.shutdownNow();
        		}
    			throw new JetelRuntimeException("Unable to initialize DataWriterDefaultStream. "+ e.getMessage());
    		}
    		
        }
        else {
        	throw new JetelRuntimeException("Unsupported processing type: "+processingType);
        }

        /* Record we use for reading from input port 0. */
		DataRecord record;
		
		/* Read records from input port 0. */
        while ((record = readRecordFromPort(0)) != null) {
			// Global record counter
			recCounter++;
			// Get all fields from the input metadata
			DataFieldMetadata[] fields = record.getMetadata().getFields();

			// JSON Object used for storing the record
			JSONObject jsonRecord = new JSONObject();

			// Iterate over all fields of the given record
			for(int idx=0;idx<fields.length;idx++){
				//DataFieldType type = fields[idx].getDataType();
				try {
					// put column to JSONRecords; corresponding data type is resolved using GoogleCloudUtils.convertCloverDataTypeToJSON 	
					jsonRecord.put( record.getField(idx).getMetadata().getName(), 
									GoogleCloudUtils.convertCloverDataTypeToJSON(record.getField(idx).getMetadata(), record.getField(idx).getValue()));
					
				} catch (JSONException e) {
					throw new JetelRuntimeException("Unable to serialize field into JSON record. Record ID: "+recCounter+", field name: "+record.getField(idx).getMetadata().getName()+", field value: "+record.getField(idx).getValue().toString()+".\r\nError detail: "+e.getMessage());
				} catch (JetelRuntimeException e) {
					throw new JetelRuntimeException("Unable to convert field value into JSON. Record ID: "+recCounter+", field name: "+record.getField(idx).getMetadata().getName()+", field value: "+record.getField(idx).getValue().toString()+".\r\nError detail: "+e.getMessage());
				}
			}
			// Add the record to the append array
            jsonArr.put(jsonRecord);

			// If we need to get rejected records, prepare a copy of the array and add extra column for identifying the row number
            if(enableRejectInformation) {
	            try {
	            	JSONObject tmpObj = new JSONObject(jsonRecord.toString());
	            	tmpObj.put("_cloverRowNum", recCounter);
	            	jsonArrMeta.put(tmpObj);
				} catch (JSONException e1) {
					getLogger().log(Level.INFO, "Unable to serialize record to the rejected. "+e1.getMessage());
				}
            }
			i++;

			// If we have enough records (defined by BATCH_SIZE), let's send it to the Big Query table
			if(i==batchSize) {
				i = 0;
            	AppendContext appendContext = new AppendContext(jsonArr, 0, jsonArrMeta);
            	long numOfRecords = jsonArr.length();
            	jsonArr = new JSONArray();
            	jsonArrMeta = new JSONArray();
            	
            	try {
                    if(processingType.equals("defaultStream")) {
                    	writer.append(appendContext);
                    } else if(processingType.equals("committed") || processingType.equals("pending")) {
                  		getLogger().log(Level.DEBUG, "Appending offset " + offset);
                    	writer.append(appendContext, offset);
    					offset = offset + numOfRecords;
                    }
            	} catch (ExecutionException e) {
    			      // If the wrapped exception is a StatusRuntimeException, check the state of the operation.
    			      // If the state is INTERNAL, CANCELLED, or ABORTED, you can retry. For more information, see:
    			      // https://grpc.github.io/grpc-java/javadoc/io/grpc/StatusRuntimeException.html
              		getLogger().log(Level.INFO, "Failed to append records. \n" + e);
              	} catch (AppendSerializationError ase ) {
					// Unable to serialize JSON Array - return error records or throw an exception (stop the processing)
	            	if(this.enableRejectInformation) {
	            		processAppendError(ase, appendContext);
	            	}
	            	else {
	            		throw new JetelRuntimeException("Unable to append data to table - serialization error. For more details, please set Enable reject information to true, since that, you can see more details on a record level. "+ ase.getMessage());
	            	}	
					
				} catch (DescriptorValidationException | IOException | InterruptedException e ) {
					throw new JetelRuntimeException("Unable to append data to table."+ e.getMessage()+".\r\n"+e.getCause());
				}
			}
		}

		// When all input records are processed, lets append the rest of the array
		if(i>0) {
			AppendContext appendContext = new AppendContext(jsonArr, 0, jsonArrMeta);

			try {
				if(processingType.equals("defaultStream")) {
					writer.append(appendContext);
				} else if (processingType.equals("committed")) {
					writer.append(appendContext, offset);
				}
			} catch (ExecutionException e) {
			      // If the wrapped exception is a StatusRuntimeException, check the state of the operation.
			      // If the state is INTERNAL, CANCELLED, or ABORTED, you can retry. For more information, see:
			      // https://grpc.github.io/grpc-java/javadoc/io/grpc/StatusRuntimeException.html
        		getLogger().log(Level.INFO, "Failed to append records. \n" + e);
        	} catch (AppendSerializationError ase ) {
				// Unable to serialize JSON Array - return error records or throw an exception (stop the processing)
            	if(this.enableRejectInformation) {
            		processAppendError(ase, appendContext);
            	}
            	else {
            		throw new JetelRuntimeException("Unable to append data to table - serialization error. For more details, please set Enable reject information to true, since that, you can see more details on a record level. "+ ase.getMessage());
            	}	
			} catch (DescriptorValidationException | IOException | InterruptedException e) {
				throw new JetelRuntimeException("Unable to append data to table."+ e.getMessage()+".\r\n"+e.getCause());
			}
		}

        switch (processingType) {
            case "defaultStream":
                writer.cleanup();
                break;
            case "committed":
                writer.cleanup(client);
                
        		try {
                    client.shutdown();
        			if (!client.awaitTermination(10, TimeUnit.SECONDS)) {
        				client.shutdownNow();
        			}
        		} catch (Exception e) {
        			client.shutdownNow();
        		}
                break;
				// Once all streams are done, if all writes were successful, commit all of them in one request.
				// This example only has the one stream. If any streams failed, their workload may be
				// retried on a new stream, and then only the successful stream should be included in the
				// commit.
				// If the response does not have a commit time, it means the commit operation failed.
            case "pending":
                writer.cleanup(client);
                getLogger().log(Level.INFO, "Going to commit pending records in stream: " + writer.getStreamName());
                BatchCommitWriteStreamsRequest commitRequest =
                        BatchCommitWriteStreamsRequest.newBuilder()
                                .setParent(parentTable.toString())
                                .addWriteStreams(writer.getStreamName())
                                .build();
                assert client != null;
                BatchCommitWriteStreamsResponse commitResponse = client.batchCommitWriteStreams(commitRequest);
                if (!commitResponse.hasCommitTime()) {
                    for (StorageError err : commitResponse.getStreamErrorsList()) {
                        getLogger().log(Level.INFO, "Issue with commit response " + err.getErrorMessage());
                    }
                    throw new RuntimeException("Error committing the streams");
                }
                getLogger().log(Level.INFO, "Appended time: " + commitResponse.getCommitTime());
                getLogger().log(Level.INFO, "Appended and committed records successfully.");
                
        		try {
                    client.shutdown();
        			if (!client.awaitTermination(10, TimeUnit.SECONDS)) {
        				client.shutdownNow();
        			}
        		} catch (Exception e) {
        			client.shutdownNow();
        		}
        		
                break;
        }
	}
	
	private void createTableIfNeeded() {
		getLogger().log(Level.INFO, "Checking if table "+tableName+" exists.");
		if(!tableExists()) {
    		getLogger().log(Level.INFO, "Table "+tableName+" does not exist.");
			if(createTable()) {
				getLogger().log(Level.INFO, "Table created");
			}
			else {
				getLogger().log(Level.WARN, "Table was not created");
			}
		}
		else {
    		getLogger().log(Level.INFO, "Table "+tableName+" does exist.");

		}		
	}

	private void initCredentials() {
		if(pathToCredentials != null && !pathToCredentials.isEmpty()){
            try (FileInputStream serviceAccountStream = new FileInputStream(pathToCredentials)) {
                this.credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
            } catch (FileNotFoundException e) {
            	throw new JetelRuntimeException("Credentials file was not found on defined path: '" + pathToCredentials+"'. "+e.getMessage());
			} catch (IOException e) {
				throw new JetelRuntimeException("Credentials file was not able to load on path: '" + pathToCredentials+"'. "+e.getMessage());
			}
        }
		
		if(this.getCredentials() != null) {
            try {
				bigQueryWriteSettings =
				        BigQueryWriteSettings.newBuilder()
				                .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials()))
				                .build();
			} catch (IOException e) {
				throw new JetelRuntimeException("Credentials file was not able to load on path: '" + pathToCredentials+"'. "+e.getMessage());
			}
        }
        else{
            try {
				bigQueryWriteSettings = BigQueryWriteSettings.newBuilder().build();
			} catch (IOException e) {
				throw new JetelRuntimeException("Credentials file was not able to load on path: '" + pathToCredentials+"'. "+e.getMessage());
			}
        }
    }

	private boolean truncate() {
		boolean truncated = false;

		if(!this.truncateTable) {
			return false;
		}

		try {
			int numRows = writer.getNumRows();
			getLogger().log(Level.INFO, "Number of records in table: "+writer.getNumRows());
			if(numRows>0) {
				long ret = writer.truncate();
				getLogger().log(Level.INFO, "Table was truncated. "+ret);
				getLogger().log(Level.INFO, "Number of records in table: "+writer.getNumRows());
				truncated = true;
			}
		} catch (JobException | InterruptedException e2) {
			getLogger().log(Level.INFO, "Unable to get number of records in table. "+e2.getMessage());
			throw new JetelRuntimeException("Unable to get number of records in table when trying to truncate table.");
		}
		return truncated;
	}
	
	private boolean createTable() {
		boolean created = false;
		List<Field> sqlFields = new LinkedList<Field>();
		
		DataRecord inputPort = inRecords[0];
		
		DataRecordMetadata inputMetadata = inputPort.getMetadata();
		DataFieldMetadata[] fields = inputMetadata.getFields() ;
		
		// Iterate over all fields of the given record
        for (DataFieldMetadata fieldMetadata : fields) {
            sqlFields.add(Field.of(fieldMetadata.getName(), GoogleCloudUtils.convertCloverDataTypeToSQL(fieldMetadata)));
        }
		Schema schema = Schema.of(sqlFields);
		
		try {
		    BigQuery bigquery = BigQueryOptions.newBuilder().setCredentials(this.credentials).build().getService();
	
		    TableId tableId = TableId.of(this.datasetName, this.tableName);
		    TableDefinition tableDefinition = StandardTableDefinition.of(schema);
		    TableInfo tableInfo = TableInfo.newBuilder(tableId, tableDefinition).build();
		    
			DatasetId datasetId = DatasetId.of(projectId, datasetName);

		    Table table = bigquery.create(tableInfo);
		    
		    Page<Table> tables = bigquery.listTables(datasetId, TableListOption.pageSize(100));
			tables.iterateAll().forEach(tab -> getLogger().log(Level.INFO, (tab.getTableId().getTable() + "\n")));

			created= table.exists();
		} catch (BigQueryException e) {
		      System.out.println("Table was not created. \n" + e.getMessage());
			  throw new JetelRuntimeException("Table "+this.tableName+" was not created." + e.getMessage());
		}
		
		return created;
	}
	
	public boolean tableExists() {
	    boolean ret = false;
		try {
		  BigQuery bigquery = BigQueryOptions.newBuilder().setCredentials(this.credentials).build().getService();

	      Table table = bigquery.getTable(TableId.of(this.datasetName, this.tableName));
	      if (table != null && table.exists()) {
	        ret = true;
	        getLogger().log(Level.INFO, "Table already exist");
	      }
	    } catch (BigQueryException e) {
	      ret = false;
	        getLogger().log(Level.INFO, "Table not found");
	    }
		return ret;
	}
	
	private void processAppendError(AppendSerializationError ase, AppendContext appendContext ) {
		Map<Integer, String> rowIndexToErrorMessage = ase.getRowIndexToErrorMessage();
		if (!rowIndexToErrorMessage.isEmpty()) {
            for (int j = 0; j < appendContext.data.length(); j++) {
                if (!rowIndexToErrorMessage.containsKey(j)) {
                	JSONObject wrongRecord;
					try {
						wrongRecord = (JSONObject) appendContext.metadata.get(j);
						this.processError(wrongRecord, "Unable to upload row number "+wrongRecord.get("_cloverRowNum") +". Reason: the record is part of the failing batch. Record details: "+appendContext.data.get(j).toString(), (int) wrongRecord.get("_cloverRowNum"));
					} catch (JSONException e) {
						getLogger().log(Level.DEBUG, "Unable to serialize record to the error port. Record details: ");
					}
				} else {
                	// process faulty rows
            		try {
                    	JSONObject wrongRecord = (JSONObject) appendContext.metadata.get(j);
						this.processError(wrongRecord, "Unable to upload row number "+wrongRecord.get("_cloverRowNum") +". Reason: "+rowIndexToErrorMessage.get(j)+". Record details: "+appendContext.data.get(j).toString(), (int) wrongRecord.get("_cloverRowNum"));
					} catch (JSONException e) {
						getLogger().log(Level.INFO, "Reporting error record failed");
					}
                }
            }
        }
	}
	
	// Write to error port (if it is connected)
	public void processError(JSONObject failingRecord, String errorMessage, int recordId) {
		if(checkErrorPortConnected() && enableRejectInformation) {
			if(checkErrorPortHasAssignedMetadata()) {
				// map error port
				DataRecord outRecord = outRecords[0];
				
				for(int i = 0; i<outRecord.getNumFields();i++) {
					DataField dataField = outRecord.getField(i);
					// propagate auto-filling fields
					if(dataField.getMetadata().isAutoFilled()){
						String autoFillingValue = dataField.getMetadata().getAutoFilling();

                        switch (autoFillingValue) {
                            case "global_row_count":
                                outRecord.getField(i).setValue(recordId);
                                break;
                            case "ErrText":
                                outRecord.getField(i).setValue(errorMessage);
                                break;
                        }
					}
					else {
						if(failingRecord.has(outRecord.getField(i).getMetadata().getName())) {
							// do the star mapping - all input fields to output fields if there are the same name
							Object field;
							try {
								field = failingRecord.get(outRecord.getField(i).getMetadata().getName());
								DataField out  = GoogleCloudUtils.jsonObjectToCloverDataField(outRecord.getField(i), field);
								outRecord.getField(i).setValue(out);
							} catch (JSONException e) {
								getLogger().log(Level.DEBUG, "Unable to get data from error field. Ignoring the field value. Error details: "+e.getMessage());
							}
						}
					}
					
				}
				// Write output record to output port 0
				writeRecordToPort(0, outRecord);
			}
			else {
				getLogger().log(Level.INFO, "Issue with record ID: "+recordId);
			}		
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public ConfigurationStatus checkConfig(ConfigurationStatus status) {
		super.checkConfig(status);

		/* This way you can check connected edges and their metadata. */
		if (getComponent().getInPorts().isEmpty()) {
			status.add("Input port must be connected!", Severity.ERROR, getComponent(), Priority.NORMAL);
			return status;
		}

		DataRecordMetadata inMetadata = getComponent().getInputPort(0).getMetadata();
		if (inMetadata == null) {
			status.add("Metadata on input port not specified!", Severity.ERROR, getComponent(), Priority.NORMAL);
			return status;
		}

		return status;
	}

	@Override
	public void init() {
		super.init();

		// check if the output port (error port) is connected
		checkErrorPortConnected();

		// check if there are metadata assigned by user (we will map the requested fields only)
		checkErrorPortHasAssignedMetadata();
	}

	public boolean checkErrorPortConnected(){
        this.isErrorPortConnected = !getComponent().getOutPorts().isEmpty();
		return this.isErrorPortConnected;
	}

	public boolean checkErrorPortHasAssignedMetadata(){
		if(checkErrorPortConnected()){
			DataRecordMetadata outMetadata = getComponent().getOutputPort(0).getMetadata();
            this.hasErrorPortAssignedMetadata = outMetadata != null;
		}
		return this.hasErrorPortAssignedMetadata;
	}

	@Override
	public void preExecute() throws ComponentNotReadyException {
		super.preExecute();
	}

	@Override
	public void postExecute() throws ComponentNotReadyException {
		super.postExecute();
	}
	
	public BigQueryWriteSettings getBigQueryWriteSettings() {
		return bigQueryWriteSettings;
	}

	public GoogleCredentials getCredentials() {
		return credentials;
	}
}