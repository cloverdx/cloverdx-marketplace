package com.cloverdx.libraries.gcloud;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryException;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Field;
import com.google.cloud.bigquery.FieldList;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.storage.v1.BigQueryWriteSettings;
import org.apache.log4j.Level;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.JetelRuntimeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BigQueryGetTableSchema extends AbstractGenericTransform {

	private String projectId;
	private String datasetName;
	private String tableName;
	private String pathToCredentials ;
	private GoogleCredentials credentials;
	private BigQueryWriteSettings bigQueryWriteSettings;

	private boolean failOnNonExists = false;
	@Override
	public void execute() {

		DataRecord record = outRecords[0];

		projectId = 				getProperties().getStringProperty("PROJECT_ID");
		datasetName = 				getProperties().getStringProperty("DATASET_NAME");
		tableName = 				getProperties().getStringProperty("TABLE_NAME");
		pathToCredentials = 		getProperties().getStringProperty("PATH_TO_CREDENTIALS");
		failOnNonExists = 			getProperties().getBooleanProperty("FAIL_ON_NON_EXISTS");

		if(projectId == null || datasetName == null || tableName ==null){
			throw new JetelRuntimeException("Please provide all required attributes (Project ID, Dataset name, Table name)");
		}

		getLogger().log(Level.INFO, "Initializing Get Table Schema.\nProject ID: "+projectId+"\nDataset name: "+datasetName+"\nTable name: "+tableName);

		initCredentials();

		try {
			BigQuery bigquery = BigQueryOptions.newBuilder().setCredentials(this.credentials).build().getService();
			Table table = bigquery.getTable(TableId.of(this.datasetName, this.tableName));
			if (table == null || !table.exists()) {
				getLogger().log(Level.WARN, "Table does not exist");

				if(failOnNonExists){
					throw new JetelRuntimeException("Table does not exist");
				}
			}
			else {
				Schema schema = table.getDefinition().getSchema();
				FieldList fieldList = schema.getFields();
				getLogger().log(Level.INFO, "Table schema: " + schema);
				getLogger().log(Level.INFO, "Table description: " + table.getDescription());
				JSONObject jsonSchema = new JSONObject();
				JSONArray fields = new JSONArray();

				for (int i = 0; i < fieldList.size(); i++) {
					Field field = fieldList.get(i);
					JSONObject jsonField = new JSONObject();
					jsonField.put("name", field.getName());
					jsonField.put("description", field.getDescription());
					jsonField.put("type", field.getType());
					jsonField.put("maxLength", field.getMaxLength());
					jsonField.put("scale", field.getScale());
					jsonField.put("precision", field.getPrecision());
					jsonField.put("subFields", field.getSubFields());
					jsonField.put("mode", field.getMode());
					jsonField.put("policyTags", field.getPolicyTags());
					jsonField.put("defaultValueExpression", field.getDefaultValueExpression());
					jsonField.put("collation", field.getCollation());
					fields.put(jsonField);
				}
				jsonSchema.put("schema", fields);
				getLogger().log(Level.INFO, "Table description JSON: " + jsonSchema);
				//String jsonTest = new Gson().toJson(schema);

				record.getField("schema").setValue((String) jsonSchema.toString(2));
				writeRecordToPort(0, record);
				record.reset();
			}
		} catch (BigQueryException e) {
			System.out.println("Unable to get table. \n" + e.toString());
			if(failOnNonExists){
				throw new JetelRuntimeException("Unable to get table.");
			}
		} catch (JSONException e) {
			throw new JetelRuntimeException("Unable to create JSON output. "+e.getMessage());

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
