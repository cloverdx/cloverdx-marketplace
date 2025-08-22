package com.cloverdx.libraries.gcloud.BigQueryReader;

import com.cloverdx.libraries.gcloud.GoogleCloudUtils;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.storage.v1.*;
import org.apache.avro.LogicalType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.jetel.component.GenericMetadataProvider;
import org.jetel.exception.JetelRuntimeException;
import org.jetel.metadata.DataFieldMetadata;
import org.jetel.metadata.DataRecordMetadata;
import org.jetel.metadata.DataRecordParsingType;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class MetadataProvider2 extends GenericMetadataProvider {
	private String projectId;
	private String datasetName;
	private String tableName;
	private String pathToCredentials ;
	private String rowRestriction ;
	private String selectedFields;
	private String[] selectedFieldsList;
	private boolean propageteMetadata;

	private GoogleCredentials credentials;
	private BigQueryReadSettings bigQueryReadSettings;
	@Override
	public void propagateMetadata() {
		
		if (getComponent().getAttributes().getProperty("PROPAGATE_METADATA")!=null) {
			propageteMetadata = Boolean.parseBoolean(getComponent().getAttributes().getProperty("PROPAGATE_METADATA"));
		}
		else {
			propageteMetadata = true;
		}
		
		if (getComponent().getOutPorts().size() == 1 && propageteMetadata) {


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
			
			DataRecordMetadata outputMetadata = new DataRecordMetadata(datasetName+"_"+tableName+"_metadata", DataRecordParsingType.DELIMITED);
			outputMetadata.setRecordDelimiter("\n\r");
			outputMetadata.setFieldDelimiter(";");
			
			try {
				BigQuery bigquery = BigQueryOptions.newBuilder().setCredentials(this.credentials).build().getService();

				Table table = bigquery.getTable(TableId.of(this.datasetName, this.tableName));
				if (table == null || !table.exists()) {
					getComponent().getGraph().getLog().log(Level.WARN, "Table does not exist");
				}
				else {
					com.google.cloud.bigquery.Schema schema = table.getDefinition().getSchema();
					FieldList fieldList = schema.getFields();
					getComponent().getGraph().getLog().log(Level.INFO, "Table schema: " + schema);
					getComponent().getGraph().getLog().log(Level.INFO, "Table description: " + table.getDescription());
					
					for (int i = 0; i < fieldList.size(); i++) {
						Field field = fieldList.get(i);
						
						if(selectedFieldsList != null && selectedFieldsList.length>0 && !Arrays.asList(selectedFieldsList).contains(field.getName())){
							continue;
						}
						LegacySQLTypeName type = field.getType();
						DataFieldMetadata newRecordField = new DataFieldMetadata(field.getName(), GoogleCloudUtils.legacySQLTypeNameToCloverDataField(type), null);
						outputMetadata.addField(newRecordField);
						
					}
					setOutputMetadata(0, outputMetadata); // Propagate metadata to output port.
				}


			} catch (BigQueryException e) {
				System.out.println("Unable to get table. \n" + e.toString());
			}
		}
	}
	
	public org.apache.avro.Schema.Type convertToAvroType(LegacySQLTypeName bigqueryType) {
		  for(org.apache.avro.Schema.Type avroType : org.apache.avro.Schema.Type.values()) {
			  if(bigqueryType.name().startsWith(avroType.name())) {
				  return avroType;
			  }
		  }
		  return null;
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
