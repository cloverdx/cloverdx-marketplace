package com.cloverdx.consulting.metadatafactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetel.component.GenericMetadataProvider;
import org.jetel.graph.GraphParameters;
import org.jetel.metadata.DataFieldMetadata;
import org.jetel.metadata.DataFieldType;
import org.jetel.metadata.DataRecordMetadata;
import org.jetel.metadata.DataRecordMetadataXMLReaderWriter;
import org.jetel.util.property.RefResFlag;

import com.cloverdx.consulting.metadatafactory.filters.ComparisonFilter;
import com.cloverdx.consulting.metadatafactory.filters.NamingFilter;

/**
 * This is an example of custom metadata propagation for a component.
 * 
 * It shows you how to add a field to any metadata coming from input port and propagate the modified metadata to output.
 */
public class MetadataBuilder extends GenericMetadataProvider {
	
	enum AdditionMode {
		PREPEND,APPEND
	}
	
	enum FieldProperty {
		NAME,TYPE,LENGTH,SCALE,SIZE,DELIMITER,NULLVALUE
	}
	
	public enum FieldAction {
		KEEP(true),KEEP_SIMILAR(true),REMOVE,REMOVE_SIMILAR,ALTER;
		
		// Action should invert selection process?
		public boolean invertSelection;
		
		FieldAction() {
			this(false);
		}
		
		FieldAction(boolean inv) {
			this.invertSelection = inv;
		}
	}

	@Override
	public void propagateMetadata() {
		GraphParameters params = getGraph().getGraphParameters();
		AdditionMode additionMode = AdditionMode.valueOf(params.getGraphParameter("ADDITION_MODE").getValueResolved(RefResFlag.REGULAR));
		FieldAction action = FieldAction.valueOf(params.getGraphParameter("FIELD_ACTION").getValueResolved(RefResFlag.REGULAR));
		
		String query = params.getGraphParameter("FIELD_FILTER").getValueResolved(RefResFlag.REGULAR);
		String name = params.getGraphParameter("NAME").getValueResolved(RefResFlag.REGULAR).trim();
		String path = params.getGraphParameter("EXT_METADATA").getValueResolved(RefResFlag.REGULAR);
		String nextRec = params.getGraphParameter("RECORD_DELIMITER").getValueResolved(RefResFlag.REGULAR);
		String delim = params.getGraphParameter("FIELD_DELIMITER").getValueResolved(RefResFlag.REGULAR);
		String properties = params.getGraphParameter("FIELD_PROPS").getValueResolved(RefResFlag.REGULAR);
		
		DataRecordMetadata additionMetadata = loadExternalMetadata(path);
		
		if (getMetadataFromInputPort(0) != null) {
			DataRecordMetadata fmt = getMetadataFromInputPort(0).duplicate();
			Collection<DataFieldMetadata> filteredFields = filterByCondition(action,fmt,query);
			// 
			if (action == FieldAction.ALTER) {
				changeFieldProperties(filteredFields,parseProperties(properties));
			} else {
				dropFields(fmt,filteredFields);
			}
			// Handle new FMT name
			if (name.isEmpty()) {
				fmt.setName(fmt.getName() + "_modified");
			} else {
				fmt.setName(name);
			}
			// Override delimiter
			if (! delim.isEmpty()) {
				fmt.setFieldDelimiter(delim);
				fmt.setEofAsDelimiter(true);
			}
			// Override record terminator
			if (! nextRec.isEmpty()) {
				fmt.setRecordDelimiter(nextRec);
			}
			// 
			if (additionMetadata != null) {
				int i = 0;
				for (DataFieldMetadata f: additionMetadata.getFields()) {
					if (additionMode == AdditionMode.PREPEND) {
						fmt.addField(i,f);
					} else {
						fmt.addField(f);
					}
					
					i++;
				}
			}
			// 
			// Propagate the modified metadata to output port.
			setOutputMetadata(0,fmt);
		}
	}

	private DataRecordMetadata loadExternalMetadata(String path) {
		if (path != null && !path.isEmpty()) {
			try {
				File addMetadata = getFile(path);
				// 
				// When metadata file is provided, load its definition
				if (addMetadata != null) {
					try (InputStream is = new FileInputStream(addMetadata)) {
						return DataRecordMetadataXMLReaderWriter.readMetadata(is);
					} catch (IOException e) {
						getComponent().getLog().warn("Unable to read metadata file: " + addMetadata.getAbsolutePath(),e);
					}
				}
			} catch (Exception e) {
				getComponent().getLog().warn("Invalid metadata file URL: " + path);
			}
		}
		
		return null;
	}
	
	public Collection<DataFieldMetadata> filterByCondition(FieldAction action, DataRecordMetadata fmt, String query) {
		List<DataFieldMetadata> fields = new ArrayList<>();
		IMetadataFilterCondition cond = null;
		
		switch (action) {
		case KEEP:
		case REMOVE:
		case ALTER:
			cond = new NamingFilter(action,query);
			break;
		case KEEP_SIMILAR:
		case REMOVE_SIMILAR:
			cond = new ComparisonFilter(action,fmt,query);
			break;
		}
		// 
		for (DataFieldMetadata d: fmt.getFields()) {
			if (cond.filter(d) ^ cond.getAction().invertSelection) {
				fields.add(d);
			}
		}
		// 
		return fields;
	}
	
	public Map<String,String> parseProperties(String properties) {
		Map<String,String> props = new HashMap<>();
		Matcher m = Pattern.compile("([^=]+)=([^\n]*)\n?").matcher(properties);
		
		while (m.find()) {
			props.put(m.group(1).trim(),m.group(2).trim());
		}
		
		return props;
	}
	
	public void dropFields(DataRecordMetadata fmt, Iterable<DataFieldMetadata> fields) {
		for (DataFieldMetadata d: fields) {
			fmt.delField(d.getName());
		}
	}
	
	public void changeFieldProperties(Iterable<DataFieldMetadata> fields, Map<String,String> props) {
		for (DataFieldMetadata f: fields) {
			Iterator<Entry<String,String>> iter = props.entrySet().iterator();
			
			while (iter.hasNext()) {
				Entry<String,String> p = iter.next();
				
				try {
					switch (FieldProperty.valueOf(p.getKey().toUpperCase())) {
						case NAME: f.setName(p.getValue().replaceAll("\\{name\\}",f.getName())); break; 
						case TYPE: f.setDataType(DataFieldType.valueOf(p.getValue().toUpperCase())); break;
						case DELIMITER: f.setDelimiter(p.getValue()); break;
						case LENGTH: f.setProperty("length",p.getValue()); break;
						case SCALE: f.setProperty("scale",p.getValue()); break;
						case SIZE: f.setSize(Integer.parseInt(p.getValue())); break;
						case NULLVALUE: f.setNullValue(p.getValue()); break;
						default: f.setProperty(p.getKey(),p.getValue()); break;
					}
				} catch (IllegalArgumentException e) {
					f.setProperty(p.getKey(),p.getValue());
				}
			}
		}
	}
}
