package com.cloverdx.consulting.metadatafactory;

import org.jetel.metadata.DataFieldMetadata;

import com.cloverdx.consulting.metadatafactory.MetadataBuilder.FieldAction;

public interface IMetadataFilterCondition {
	public FieldAction getAction();
	public boolean filter(DataFieldMetadata field);
}
