package com.cloverdx.libraries.gcloud;

import org.jetel.component.GenericMetadataProvider;
import org.jetel.graph.TransformationGraph;
import org.jetel.metadata.DataFieldMetadata;
import org.jetel.metadata.DataFieldType;
import org.jetel.metadata.DataRecordMetadata;
import org.jetel.metadata.DataRecordParsingType;

public class BigQueryExecuteCommandMetadataProvider extends GenericMetadataProvider {

	@Override
	public void propagateMetadata() {
		/** Accessing graph. */
		TransformationGraph graph = getGraph();
		
		boolean returnResults = graph.getGraphParameters().asProperties().getBooleanProperty("_RETURN__RESULTS");
		
		DataRecordMetadata customMetadata = new DataRecordMetadata("BigQueryExecuteCommend_Output", DataRecordParsingType.DELIMITED);
		customMetadata.setRecordDelimiter("\n");
		customMetadata.setFieldDelimiter("|");
		
		if(!returnResults) {
			DataFieldMetadata updateCount = new DataFieldMetadata("updateCount", DataFieldType.LONG, null);
			DataFieldMetadata statement = new DataFieldMetadata("statement", DataFieldType.STRING, null);
			customMetadata.addField(updateCount);
			customMetadata.addField(statement);

			setOutputMetadata(0, customMetadata); // Propagate metadata to output port.
		}
		else {
			DataFieldMetadata value = new DataFieldMetadata("value", DataFieldType.STRING, null);
			customMetadata.addField(value);
			setOutputMetadata(0, customMetadata); // Propagate metadata to output port.
		}
	}
}