package com.cloverdx.consulting.metadatafactory.filters;

import org.jetel.metadata.DataFieldMetadata;
import org.jetel.metadata.DataRecordMetadata;

import com.cloverdx.consulting.metadatafactory.IMetadataFilterCondition;
import com.cloverdx.consulting.metadatafactory.MetadataBuilder.FieldAction;

public class ComparisonFilter implements IMetadataFilterCondition {

	private DataFieldMetadata[] fieldList;
	private FieldAction action;
	
	public ComparisonFilter(FieldAction action, DataRecordMetadata record, String query) {
		String[] fieldNames = query.split("[,;]");
		this.fieldList = new DataFieldMetadata[fieldNames.length];
		// Get field settings
		for (int i=0;i<fieldNames.length;i++) {
			this.fieldList[i] = record.getField(fieldNames[i]);
		}
		// 
		this.action = action;
	}
	
	public boolean isSimilar(DataFieldMetadata f1, DataFieldMetadata f2) {
		return f1.getDataType().equals(f2.getDataType());
	}
	
	@Override
	public FieldAction getAction() {
		return this.action;
	}

	@Override
	public boolean filter(DataFieldMetadata field) {
		boolean similar = false;
		// 
		for (int i=0;i<fieldList.length && !similar;i++) {
			similar = fieldList[i] != null && isSimilar(fieldList[i],field);
		}
		// 
		return similar;
	}

}
