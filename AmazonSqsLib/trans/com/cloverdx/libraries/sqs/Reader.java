package com.cloverdx.libraries.sqs;

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Level;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.metadata.DataRecordMetadata;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.cloverdx.libraries.sqs.request.AbstractReaderConnector;
import com.cloverdx.libraries.sqs.request.ISqsConnector;
import com.cloverdx.libraries.sqs.request.PendingMessageConnector;
import com.cloverdx.libraries.sqs.request.QueueStatusConnector;

/**
 * This is an example custom reader. It shows how you can
 *  create records using a data source.
 */
public class Reader extends AbstractGenericTransform {
	
	private final static String MSG_ID = "id"; 
	private final static String MSG_BODY = "body";
	private final static String MSG_RECEIPT_HANDLE = "receiptHandle";
	private final static String ATTR_MSG_ID = "messageId";
	private final static String ATTR_TYPE = "dataType";
	private final static String ATTR_NAME = "name";
	private final static String ATTR_VALUE = "value";
	private final static String ATTR_BYTE = "byteValue";

	String awsAccessKey = null;
	String awsSecretKey = null;
	String sqsQueueName = null;
	String sqsQueueURL = null;
	String sqsRegion = null;
	Boolean removeReadMessages = true;
	Boolean sqsQueueStatus = false;
	int sqsWaitTime = -1;
	int sqsVisibilityTimeout = -1;
	int sqsMaxMessages = -1;
	int sqsBatchSize = AbstractReaderConnector.BATCH_SIZE;
	
	Integer[] msgFields;
	Integer[] attrFields;
	
	ISqsConnector request;
	
	@Override
	public void execute() throws ComponentNotReadyException {
		DataRecord messageOut = outRecords[0];
		DataRecord attributeOut = outRecords[1];
	    
		Iterator<Message> msgQueue = request.iterator();
		while (msgQueue.hasNext() && getComponent().runIt()) {
			Message message = msgQueue.next();
			String messageID = message.getMessageId();
			getLogger().info("Reading message: " + messageID);
				    
			// Write out message contents
			messageOut.getField(msgFields[0]).setValue(messageID);
			messageOut.getField(msgFields[1]).setValue(message.getBody());
			messageOut.getField(msgFields[2]).setValue(message.getReceiptHandle());
			writeRecordToPort(0,messageOut);
			messageOut.reset();
			
			// Outputs all message attributes through out[1] port
			for (Map.Entry<String, MessageAttributeValue> entry : message.getMessageAttributes().entrySet()) {
				attributeOut.getField(attrFields[0]).setValue(messageID);
				attributeOut.getField(attrFields[1]).setValue(entry.getValue().getDataType());
				attributeOut.getField(attrFields[2]).setValue(entry.getKey());
				attributeOut.getField(attrFields[3]).setValue(entry.getValue().getStringValue());
				// Slightly different treatment of binary attributes
				if (entry.getValue().getBinaryValue() != null)
					attributeOut.getField(attrFields[4]).setValue(entry.getValue().getBinaryValue().array());
						
				getLogger().log(Level.INFO, "Message attribute: " + entry.getKey() + "=" + entry.getValue().toString());
				writeRecordToPort(1,attributeOut);
				attributeOut.reset();
			}
		}
	}
	
	@Override
	public ConfigurationStatus checkConfig(ConfigurationStatus status) {
		super.checkConfig(status);
		
		if (getComponent().getOutPorts().size() < 1) {
			status.addError(this.getComponent(), "Output port 0", "Edge not connected!");
			return status;
		}

		DataRecordMetadata outMetadata = getComponent().getOutputPort(0).getMetadata();
		if (outMetadata == null) {
			status.addError(this.getComponent(), "Output port 0", "Metadata not specified!");
			return status;
		}

		if (outMetadata.getFieldPosition("id") == -1) {
			status.addError(this.getComponent(), "Output port 0", "Incompatible metadata!");
		}
		
		return status;
	}

	@Override
	public void init() {
		super.init();
		awsAccessKey = getProperties().getStringProperty("AWSAccessKey");
		awsSecretKey = getProperties().getStringProperty("AWSSecretKey");
		sqsQueueName = getProperties().getStringProperty("SQSQueueName");
		sqsRegion = getProperties().getStringProperty("SQSRegion");
		
		if (getProperties().getProperty("SQSMaxMessages") != null)
			sqsMaxMessages = getProperties().getIntProperty("SQSMaxMessages");
		
		if (getProperties().getProperty("SQSWaitTime") != null)
			sqsWaitTime = getProperties().getIntProperty("SQSWaitTime");
		
		if (getProperties().getProperty("SQSVisibilityTimeout") != null)
			sqsVisibilityTimeout = getProperties().getIntProperty("SQSVisibilityTimeout");
		
		if (getProperties().getProperty("SQSRemoveReadMessages") != null)
			removeReadMessages = getProperties().getBooleanProperty("SQSRemoveReadMessages");
		
		if (getProperties().getProperty("SQSGetQueueStatus") != null)
			sqsQueueStatus = getProperties().getBooleanProperty("SQSGetQueueStatus");

		DataRecordMetadata msgMeta = getComponent().getOutMetadata().get(0);
		DataRecordMetadata attrMeta = getComponent().getOutMetadata().get(1);
		
		msgFields = new Integer[] { 
			msgMeta.getFieldPosition(MSG_ID),
			msgMeta.getFieldPosition(MSG_BODY),
			msgMeta.getFieldPosition(MSG_RECEIPT_HANDLE)
		};
		
		attrFields = new Integer[] {
			attrMeta.getFieldPosition(ATTR_MSG_ID),
			attrMeta.getFieldPosition(ATTR_TYPE),
			attrMeta.getFieldPosition(ATTR_NAME),
			attrMeta.getFieldPosition(ATTR_VALUE),
			attrMeta.getFieldPosition(ATTR_BYTE)
		};	    
	}

	@Override
	public void preExecute() throws ComponentNotReadyException {
		super.preExecute();
		
		try{
			
	    	AWSCredentials auth;
	    	
	    	// If credentials provided explicitly, create new authentication object
	    	if (awsAccessKey != null && awsSecretKey != null) {
	    		auth = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
	    		
	    	// Otherwise, let SDK decide
	    	} else {
	    		auth = null;
	    	}
	    	
	    	// For standard operation = get messages from queue
	    	if (!sqsQueueStatus) {
	    		request = new PendingMessageConnector(auth,sqsRegion,sqsQueueName,sqsBatchSize,sqsMaxMessages,sqsWaitTime,sqsVisibilityTimeout);
	    		
	    		if (removeReadMessages) {
	    			((PendingMessageConnector) request).setAutoAck();
	    		}

	    	// Otherwise request queue status information
	    	} else {
	    		request = new QueueStatusConnector(auth,sqsRegion,sqsQueueName);
	    	}
	    	
	    } catch(Exception e){
	    	throw new ComponentNotReadyException("Unable to connect to Amazon SQS!",e);
	    }
	}

	@Override
	public void postExecute() throws ComponentNotReadyException {
		super.postExecute();
		
		if (request != null) {
			request.close();
		}
	}
}
