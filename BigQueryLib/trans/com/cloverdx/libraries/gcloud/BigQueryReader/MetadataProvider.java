package com.cloverdx.libraries.gcloud.BigQueryReader;

import com.cloverdx.libraries.gcloud.GoogleCloudUtils;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.storage.v1.*;
import org.apache.avro.LogicalType;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.jetel.component.GenericMetadataProvider;
import org.jetel.exception.JetelRuntimeException;
import org.jetel.metadata.DataFieldMetadata;
import org.jetel.metadata.DataFieldType;
import org.jetel.metadata.DataRecordMetadata;
import org.jetel.metadata.DataRecordParsingType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MetadataProvider extends GenericMetadataProvider {
	private String projectId;
	private String datasetName;
	private String tableName;
	private String pathToCredentials ;
	private String rowRestriction ;
	private String selectedFields;
	private String[] selectedFieldsList;
	private GoogleCredentials credentials;
	private BigQueryReadSettings bigQueryReadSettings;
	@Override
	public void propagateMetadata() {
		// Create metadata for the error port
		// If input metadata connected, copy the whole metadata and add error fields
		if (getComponent().getOutPorts().size() == 1 ) {


			projectId = 				getComponent().getAttributes().getProperty("PROJECT_ID");
			datasetName = 				getComponent().getAttributes().getProperty("DATASET_NAME");
			tableName = 				getComponent().getAttributes().getProperty("TABLE_NAME");
			pathToCredentials = 		getComponent().getAttributes().getProperty("PATH_TO_CREDENTIALS");
			rowRestriction = 		    getComponent().getAttributes().getProperty("ROW_RESTRICTION");
			selectedFields = 		    getComponent().getAttributes().getProperty("SELECTED_FIELDS");

			if(projectId == null || datasetName == null || tableName ==null){
				throw new JetelRuntimeException("Unable to propagate metadata, it is necessary to provide all required attributes (Project ID, Dataset name, Table name)");
			}

			if(selectedFields != null && !StringUtils.isEmpty(selectedFields)){
				selectedFieldsList = selectedFields.split(",");
			}

			getComponent().getGraph().getLog().log(Level.INFO, "Initializing reader metadata provider.\nProject ID: "+projectId+"\nDataset name: "+datasetName+"\nTable name: "+tableName+"\nSelected fields: "+ Arrays.toString(selectedFieldsList));

			initCredentials();

			BigQueryReadClient client = null;
			String srcTable = String.format("projects/%s/datasets/%s/tables/%s", projectId, datasetName, tableName);
			String parent = String.format("projects/%s", projectId);

			try {
				client = BigQueryReadClient.create(this.getBigQueryReadSettings());
			} catch (IOException e) {
				throw new JetelRuntimeException("Unable to create BigQueryReadClient. "+e.getMessage());
			}

			ReadSession.TableReadOptions options =
					ReadSession.TableReadOptions.newBuilder()
							.build();

			ReadSession.Builder sessionBuilder =
					ReadSession.newBuilder()
							.setTable(srcTable)
							// This API can also deliver data serialized in Apache Avro format.
							// This example leverages Apache Avro.
							.setDataFormat(DataFormat.AVRO)
							.setReadOptions(options);

			CreateReadSessionRequest.Builder builder =
					CreateReadSessionRequest.newBuilder()
							.setParent(parent)
							.setReadSession(sessionBuilder)
							.setMaxStreamCount(1);

			// Request the session creation.
			ReadSession session = client.createReadSession(builder.build());
			Schema schema = new Schema.Parser().parse(session.getAvroSchema().getSchema());

			//System.out.println("Schema for "+srcTable+" is "+ schema.toString());

			DataRecordMetadata outputMetadata = new DataRecordMetadata(datasetName+"_"+tableName+"_metadata", DataRecordParsingType.DELIMITED);
			outputMetadata.setRecordDelimiter("\n\r");
			outputMetadata.setFieldDelimiter(";");

			for(int i = 0; i < schema.getFields().size(); i++){
				Schema.Field field = schema.getFields().get(i) ;

				if(selectedFieldsList != null && selectedFieldsList.length>0 && !Arrays.asList(selectedFieldsList).contains(field.name())){
					continue;
				}

				String type = "STRING";

				if(field.schema().getType().getName().toUpperCase().equals("UNION")){
					java.util.List<Schema> schemaList = field.schema().getTypes();

					for(Schema s: schemaList){
						if(s != null && !s.getName().equalsIgnoreCase("null")){
							String logicalTypeName = s.getProp(LogicalType.LOGICAL_TYPE_PROP);
							if(logicalTypeName != null && !StringUtils.isEmpty(logicalTypeName)){
								type = logicalTypeName;
							}
							else if(s.getLogicalType() != null){
								type = s.getLogicalType().getName();
							}
							else{
								type = s.getType().getName();
							}
						}
					}
				}
				else{
					String logicalTypeName = field.schema().getProp(LogicalType.LOGICAL_TYPE_PROP);

					if(logicalTypeName != null && !StringUtils.isEmpty(logicalTypeName)){
						type = logicalTypeName;
					}
					if(field.schema().getLogicalType() != null){
						type = field.schema().getLogicalType().getName();
					}
					else{
						type = field.schema().getName();
					}
				}
				DataFieldMetadata newRecordField = new DataFieldMetadata(field.name(), GoogleCloudUtils.avroTypeToCloverDataField(type), null);
				outputMetadata.addField(newRecordField);
			}
			setOutputMetadata(0, outputMetadata); // Propagate metadata to output port.

			client.shutdown();
			try {
				if (!client.awaitTermination(3500, TimeUnit.MILLISECONDS)) {
					client.shutdownNow();
				}
			} catch (InterruptedException e) {
				client.shutdownNow();
			}
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
				bigQueryReadSettings =
						BigQueryReadSettings.newBuilder()
								.setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials()))
								.build();
			} catch (IOException e) {
				throw new JetelRuntimeException("Credentials file was not able to load on path: '" + pathToCredentials+"'. "+e.getMessage());
			}
		}
		else{
			try {
				bigQueryReadSettings = BigQueryReadSettings.newBuilder().build();
			} catch (IOException e) {
				throw new JetelRuntimeException("Credentials file was not able to load on path: '" + pathToCredentials+"'. "+e.getMessage());
			}
		}
	}

	public GoogleCredentials getCredentials() {
		return credentials;
	}

	public BigQueryReadSettings getBigQueryReadSettings() {
		return bigQueryReadSettings;
	}
}
