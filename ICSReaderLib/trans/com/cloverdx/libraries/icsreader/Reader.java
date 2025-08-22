package com.cloverdx.libraries.icsreader;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Level;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.ConfigurationStatus.Priority;
import org.jetel.exception.ConfigurationStatus.Severity;
import org.jetel.metadata.DataRecordMetadata;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyListAccessor;
import net.fortuna.ical4j.model.component.VEvent;

/**
 * This is an example custom transformer. It shows how you can read records, process their values and write records.
 */
public class Reader extends AbstractGenericTransform {
	@Override
	public void execute() {
		/** This code is an example. Replace it with your custom code. */

		/** This is how you can read component properties. */
		//String customValue = getProperties().getStringProperty("myCustomPropertyName");

		/** You can log messages. */
		//getLogger().log(Level.DEBUG, "Custom property resolved to: " + customValue);

		/** Record prepared for reading from input port 0 */
		DataRecord inRecord = inRecords[0];

		/** Record prepared for writing to output port 0 */
		DataRecord outRecord = outRecords[0];
		
		/** Record prepared for writing to output port 0 */
		DataRecord outRecordEvents = outRecords[1];


		/** Read all records from input port 0 */
		
		while ((inRecord = readRecordFromPort(0)) != null) {
			// Get value from input record
			String valueFromRecord = inRecord.getField("content").getValue().toString();
			
			CalendarBuilder builder = new CalendarBuilder();
			StringReader sin = new StringReader(valueFromRecord);
			
			try {
				Calendar calendar = builder.build(sin);
				
				
				/*
				 * 
				 * <Field name="prodid" type="string"/>
					<Field name="version" type="string"/>
					<Field name="calscale" type="string"/>
					<Field name="method" type="string"/>
					<Field name="x_wr_calname" type="string"/>
					<Field name="x_wr_timezone" type="string"/>
					<Field name="x_wr_caldesc" type="string"/>
					<Field name="tzid" type="string"/>
					<Field name="x_lic_location" type="string"/>
					<Field name="tzoffsetfrom" type="string"/>
					<Field name="tzoffsetto" type="string"/>
					<Field name="tzname" type="string"/>
					<Field name="dtstart" type="string"/>
					<Field name="rrule" type="string"/>

				 */
				
				 // Initialize CloverDX metadata fields
	            String prodid = getCalendarProperty(calendar, Property.PRODID);
	            
				getLogger().log(Level.INFO, "PRODID: \n\n" + prodid);

				
	            outRecord.getField("prodid").setValue(prodid);
	            
	            String version = getCalendarProperty(calendar, Property.VERSION);
	            outRecord.getField("version").setValue(version);

	            String calscale = getCalendarProperty(calendar, Property.CALSCALE);
	            outRecord.getField("calscale").setValue(calscale);

	            String method = getCalendarProperty(calendar, Property.METHOD);
	            outRecord.getField("method").setValue(method);

	            String x_wr_calname = getCalendarProperty(calendar, "X-WR-CALNAME");
	            outRecord.getField("x_wr_calname").setValue(x_wr_calname);

	            String x_wr_timezone = getCalendarProperty(calendar, "X-WR-TIMEZONE");
	            outRecord.getField("x_wr_timezone").setValue(x_wr_timezone);

	            String x_wr_caldesc = getCalendarProperty(calendar, "X-WR-CALDESC");
	            outRecord.getField("x_wr_caldesc").setValue(x_wr_caldesc);

	            String tzid = getCalendarProperty(calendar, "TZID");
	            outRecord.getField("tzid").setValue(tzid);

	            String x_lic_location = getCalendarProperty(calendar, "X-LIC-LOCATION");
	            outRecord.getField("x_lic_location").setValue(x_lic_location);
	            
	            
				writeRecordToPort(0, outRecord);
				
				 // If there are events (VEVENT) in the calendar, extract event-specific fields
	            for (Component component : calendar.getComponents()) {
	                if (component instanceof VEvent) {
	                	
	                	
	                    VEvent event = (VEvent) component;

	                    // Mapping event fields to CloverDX metadata
	                    String dtstart = getComponentProperty(event, Property.DTSTART);
	                    outRecordEvents.getField("dtstart").setValue(dtstart);
	                    
	                    String dtend = getComponentProperty(event, Property.DTEND);
	                    outRecordEvents.getField("dtend").setValue(dtend);

	                    String dtstamp = getComponentProperty(event, Property.DTSTAMP);
	                    outRecordEvents.getField("dtstamp").setValue(dtstamp);

	                    String uid = getComponentProperty(event, Property.UID);
	                    outRecordEvents.getField("uid").setValue(uid);

	                    String created = getComponentProperty(event, Property.CREATED);
	                    outRecordEvents.getField("created").setValue(created);

	                    String description = getComponentProperty(event, Property.DESCRIPTION);
	                    outRecordEvents.getField("description").setValue(description);

	                    String last_modified = getComponentProperty(event, Property.LAST_MODIFIED);
	                    outRecordEvents.getField("last_modified").setValue(last_modified);

	                    String location = getComponentProperty(event, Property.LOCATION);
	                    outRecordEvents.getField("location").setValue(location);

	                    String sequence = getComponentProperty(event, Property.SEQUENCE);
	                    outRecordEvents.getField("sequence").setValue(sequence);

	                    String status = getComponentProperty(event, Property.STATUS);
	                    outRecordEvents.getField("status").setValue(status);

	                    String summary = getComponentProperty(event, Property.SUMMARY);
	                    outRecordEvents.getField("summary").setValue(summary);

	                    String transp = getComponentProperty(event, Property.TRANSP);
	                    outRecordEvents.getField("transp").setValue(transp);

	                    String rule = getComponentProperty(event, Property.RRULE);
	                    outRecordEvents.getField("rule").setValue(rule);
	                    
	    				writeRecordToPort(1, outRecordEvents);
	                	outRecordEvents.reset();

	                }
	           }

	            
	            	
			} catch (IOException | ParserException e) {
				getLogger().log(Level.INFO, "EXCEPTION: \n\n" + e);

			}

			// Write output record to output port 0
		}
		
	}

	@Override
	public ConfigurationStatus checkConfig(ConfigurationStatus status) {
		super.checkConfig(status);

		/** This way you can check connected edges and their metadata. */
		/*
		if (getComponent().getInPorts().size() < 1 || getComponent().getOutPorts().size() < 1) {
			status.addError(getComponent(), null, "Both input and output port must be connected!");
			return status;
		}

		DataRecordMetadata inMetadata = getComponent().getInputPort(0).getMetadata();
		DataRecordMetadata outMetadata = getComponent().getOutputPort(0).getMetadata();
		if (inMetadata == null || outMetadata == null) {
			status.addError(getComponent(), null, "Metadata on input or output port not specified!");
			return status;
		}

		if (inMetadata.getFieldPosition("myIntegerField") == -1) {
			status.addError(getComponent(), null, "Incompatible input metadata!");
		}
		if (outMetadata.getFieldPosition("myIntegerField") == -1) {
			status.addError(getComponent(), null, "Incompatible output metadata!");
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
	}

	@Override
	public void postExecute() throws ComponentNotReadyException {
		super.postExecute();
	}
	
	// Utility method to get a property from a Calendar
	private static String getCalendarProperty(Calendar calendar, String propertyName) {
	    // Iterating over the List<Property> to find the desired property by name
	    for (Property property : calendar.getProperties()) {
	        if (property.getName().equalsIgnoreCase(propertyName)) {
	            return property.getValue(); // Return the property value if found
	        }
	    }
	    return null; // Return null if the property is not found
	}

    // Utility method to get a property from a Component (like VEvent)
    private static String getComponentProperty(Component component, String propertyName) {
        return component.getProperty(propertyName)
                        .map(Property::getValue)   // If present, get the value
                        .orElse(null);             // Otherwise return null
    }
}
