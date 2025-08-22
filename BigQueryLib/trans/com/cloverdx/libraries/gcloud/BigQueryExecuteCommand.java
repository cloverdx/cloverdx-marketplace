package com.cloverdx.libraries.gcloud;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.*;
import com.google.cloud.bigquery.storage.v1.BigQueryWriteSettings;
import org.apache.log4j.Level;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.JetelRuntimeException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public class BigQueryExecuteCommand extends AbstractGenericTransform {

	private String projectId;
	private String datasetName;
	private String pathToCredentials ;
	private GoogleCredentials credentials;
	private BigQueryWriteSettings bigQueryWriteSettings;
	private String command ;
	private boolean failOnError = false;
	private boolean returnResults = true;

	@Override
	public void execute() {

		DataRecord record = outRecords[0];

		projectId = 				getProperties().getStringProperty("PROJECT_ID");
		datasetName = 				getProperties().getStringProperty("DATASET_NAME");
		pathToCredentials = 		getProperties().getStringProperty("PATH_TO_CREDENTIALS");
		failOnError = 				getProperties().getBooleanProperty("FAIL_ON_ERROR");
		command = 					getProperties().getStringProperty("COMMAND");
		returnResults = 			getProperties().getBooleanProperty("RETURN_RESULTS");

		// Statement from input port overrides the statement set using the component attribute.
		DataRecord inRecord = readRecordFromPort(0);
		if(inRecord != null && inRecord.hasField("statement") && inRecord.getField("statement").getValue()!=null){
			command = (String) inRecord.getField("statement").getValue().toString();
		}

		if(projectId == null || datasetName == null  || command == null){
			throw new JetelRuntimeException("Please provide all required attributes (Project ID, Dataset name, Command)");
		}

		getLogger().log(Level.INFO, "Initializing Execute Command.\nProject ID: "+projectId+"\nDataset name: "+datasetName+"\nCommand: "+command);

		initCredentials();
		
		try {
			QueryJobConfiguration queryConfig = QueryJobConfiguration.newBuilder(command).build();
			BigQuery bigquery = BigQueryOptions.newBuilder().setCredentials(this.credentials).build().getService();

			TableResult results = bigquery.query(queryConfig);

			if(returnResults){
				for (Iterator<FieldValueList> it = results.getValues().iterator(); it.hasNext(); ) {
					FieldValueList row = it.next();
					record.getField(0).setValue((String) row.toString());
					writeRecordToPort(0, record);
					record.reset();
				}
			}
			else{
				record.getField("updateCount").setValue(results.getTotalRows());
				record.getField("statement").setValue((String) command);
				writeRecordToPort(0, record);
				record.reset();
			}
		} catch (InterruptedException ex) {
            throw new JetelRuntimeException("BigQuery Command ended with error. "+ex.getMessage());
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
	}

	public BigQueryWriteSettings getBigQueryWriteSettings() {
		return bigQueryWriteSettings;
	}

	public GoogleCredentials getCredentials() {
		return credentials;
	}

	@Override
	public ConfigurationStatus checkConfig(ConfigurationStatus status) {
		super.checkConfig(status);

		return status;
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void preExecute() throws ComponentNotReadyException {
		super.preExecute();
	}

	@Override
	public void postExecute() throws ComponentNotReadyException {
		super.postExecute();
	}
}
