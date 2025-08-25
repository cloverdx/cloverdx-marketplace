package com.cloverdx.sflibrary;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.log4j.Level;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataRecord;
import org.jetel.database.IConnection;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.JetelRuntimeException;
import org.jetel.metadata.DataRecordMetadata;
import org.jetel.metadata.DataRecordMetadataXMLReaderWriter;
import java.util.Properties;
import java.sql.SQLException;

public class ExportSalesforceMetadata extends AbstractGenericTransform {

	@Override
	public void execute() {
		
		/* Get custom component properties */
		String object = getProperties().getStringProperty("SFObject");
		String targetDir = getProperties().getStringProperty("TargetDir");	
		String targetName = getProperties().getStringProperty("TargetName");

		
		/* Get connection from graph */
		IConnection sfConnection = getGraph().getConnection("SALESFORCE");
		
		/* Properties to be used to get metadata */
		Properties props = new Properties();
		props.put("query", "SELECT FIELDS(ALL) FROM " + object);
		DataRecord record = outRecords[0];
		
		try {
			
			/* Get Metadata from Salesforce connection */
			DataRecordMetadata createMetadata = sfConnection.createMetadata(props);
			String objectFields = String.join(", ", createMetadata.getFieldNamesArray());
			
			record.getField("fields").setValue(objectFields);
			writeRecordToPort(0, record);
			
			/* And Write it in to meta Folder */
			try (OutputStream streamOut = getOutputStream(targetDir + targetName + ".fmt", false)){
				DataRecordMetadataXMLReaderWriter.write(createMetadata, streamOut);
			}
			
		}catch (IOException | SQLException e) {
			getLogger().log(Level.INFO, "Error while processing object: " + object);
			
			throw new JetelRuntimeException("Failed to extract Salesforce metadata.", e);
			
		}
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
