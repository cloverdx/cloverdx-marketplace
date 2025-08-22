package com.cloverdx.consulting.metadatafactory; 

import java.util.Iterator;

import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.graph.TransformationGraph;
import org.jetel.metadata.DataFieldMetadata;
import org.jetel.metadata.DataFieldType;
import org.jetel.metadata.DataRecordMetadata;

/**
 * This is an example custom transformer. It shows how you can read records, process their values and write records.
 */
public class Transform extends AbstractGenericTransform {

	// 
	// Contains in / out order of fields to be copied 
	private int[] mapping;
	
	@Override
	public void execute() {
		DataRecord inRecord = inRecords[0];
		DataRecord outRecord = outRecords[0];

		while (getComponent().runIt() && (inRecord = readRecordFromPort(0)) != null) {
			outRecord.reset();
			// 
			// For all copyable fields - copy
			for (int i=0;i<mapping.length;i++) {
				if (mapping[i] > -1) {
					outRecord.getField(mapping[i]).setValue(
						inRecord.getField(i)
					);
				}
			}
			
			writeRecordToPort(0,outRecord);
		}
	}

	@Override
	public ConfigurationStatus checkConfig(ConfigurationStatus status) {
		super.checkConfig(status);

		/** This way you can check connected edges and their metadata. */
		/*
		if (getComponent().getInPorts().size() < 1 || getComponent().getOutPorts().size() < 1) {
			status.add("Both input and output port must be connected!", Severity.ERROR, getComponent(), Priority.NORMAL);
			return status;
		}

		DataRecordMetadata inMetadata = getComponent().getInputPort(0).getMetadata();
		DataRecordMetadata outMetadata = getComponent().getOutputPort(0).getMetadata();
		if (inMetadata == null || outMetadata == null) {
			status.add("Metadata on input or output port not specified!", Severity.ERROR, getComponent(), Priority.NORMAL);
			return status;
		}

		if (inMetadata.getFieldPosition("myIntegerField") == -1) {
			status.add("Incompatible input metadata!", Severity.ERROR, getComponent(), Priority.NORMAL);
		}
		if (outMetadata.getFieldPosition("myIntegerField") == -1) {
			status.add("Incompatible output metadata!", Severity.ERROR, getComponent(), Priority.NORMAL);
		}
		*/
		return status;
	}

	@Override
	public void init() {
		super.init();
	}

	@Override
	public void preExecute() throws ComponentNotReadyException {
		super.preExecute();
		DataRecordMetadata in = getComponent().getInMetadata().get(0);
		DataRecordMetadata out = getComponent().getOutMetadata().get(0);
		// 
		mapping = new int[in.getFields().length];
		// 
		for (DataFieldMetadata f1: in.getFields()) {
			DataFieldMetadata f2 = out.getField(f1.getName());
			
			if (f2 != null && (f1.isSubtype(f2)  ||
            	(f1.getDataType() == DataFieldType.DECIMAL && f2.getDataType() == DataFieldType.DECIMAL))) {
            	// Configure mapping
				mapping[f1.getNumber()] = f2.getNumber();
			} else {
				mapping[f1.getNumber()] = -1;
			}
		}
	}

	@Override
	public void postExecute() throws ComponentNotReadyException {
		super.postExecute();
	}
}
