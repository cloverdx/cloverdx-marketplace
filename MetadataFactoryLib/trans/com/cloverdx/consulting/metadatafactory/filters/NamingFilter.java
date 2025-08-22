package com.cloverdx.consulting.metadatafactory.filters;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.jetel.metadata.DataFieldMetadata;

import com.cloverdx.consulting.metadatafactory.IMetadataFilterCondition;
import com.cloverdx.consulting.metadatafactory.MetadataBuilder.FieldAction;

public class NamingFilter implements IMetadataFilterCondition {
	
	private static final String REGEX_PREFIX = "regex:";
	
	private String[] fieldList = null;
	private Pattern pattern = null;
	private FieldAction action;
	
	public NamingFilter(FieldAction action, String query) {
		// Regex or list of fields?
		if (query.startsWith(REGEX_PREFIX)) {
			this.pattern = Pattern.compile(query.substring(REGEX_PREFIX.length()));
			//throw new RuntimeException("'"+this.pattern.pattern()+"'");
		} else {
			this.fieldList = query.split("[;,]");
			Arrays.sort(this.fieldList);
		}
		// 
		this.action = action;
	}

	@Override
	public FieldAction getAction() {
		return this.action;
	}

	@Override
	public boolean filter(DataFieldMetadata field) {
		boolean onList = false;
		
		if (fieldList != null) {
			onList = Arrays.binarySearch(this.fieldList,field.getName()) > -1;
		} else if (pattern != null) {
			onList = pattern.matcher(field.getName()).matches();
		}
		
		return onList;
	}

}
