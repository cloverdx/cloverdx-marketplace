package com.cloverdx.libraries.gcloud.BigQueryBulkWriter;

import org.jetel.component.GenericMetadataProvider;
import org.jetel.metadata.DataFieldMetadata;
import org.jetel.metadata.DataFieldType;
import org.jetel.metadata.DataRecordMetadata;

public class DefaultStreamWriterMetadataProvider extends GenericMetadataProvider {

	@Override
	public void propagateMetadata() {
		// Create metadata for the error port
		// If input metadata connected, copy the whole metadata and add error fields
		if (getComponent().getInPorts().size() == 1 && this.getMetadataFromInputPort(0) != null) {
			DataRecordMetadata inputMetadata = this.getMetadataFromInputPort(0);
			DataRecordMetadata outputMetadata = inputMetadata.duplicate();
			outputMetadata.setName("BQDefaultStreamWriterErrorMetadata");
			
			 
			DataFieldMetadata errorMessage = new DataFieldMetadata("errorMessage", DataFieldType.STRING, null);
			errorMessage.setAutoFilling("ErrText");
			
			DataFieldMetadata rowNum = new DataFieldMetadata("rowNum", DataFieldType.INTEGER, null);
			rowNum.setAutoFilling("global_row_count");

			outputMetadata.addField(errorMessage);
			outputMetadata.addField(rowNum);
			
			setOutputMetadata(0, outputMetadata); // Propagate metadata to output port.
		}
	}
	

}
