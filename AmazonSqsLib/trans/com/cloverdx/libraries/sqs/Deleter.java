package com.cloverdx.libraries.sqs;

import org.apache.log4j.Level;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.ConfigurationStatus.Priority;
import org.jetel.exception.ConfigurationStatus.Severity;
import org.jetel.metadata.DataRecordMetadata;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;

/**
 * This is an example custom transformer. It shows how you can read records, process their values and write records.
 */
public class Deleter extends AbstractGenericTransform {
	
	private final static String CONF_ACCESS_KEY = "AWSAccessKey";
	private final static String CONF_SECRET_KEY = "AWSSecretKey";
	private final static String CONF_QUEUE_NAME = "SQSQueueName";
	private final static String CONF_REGION = "SQSRegion";
	
	String awsAccessKey = null;
	String awsSecretKey = null;
	String sqsQueueName = null;
	String sqsQueueURL = null;
	String sqsRegion = null;
	
	AmazonSQS	sqs = null;
	
	@Override
	public void execute() throws ComponentNotReadyException {
		// Try to connect to Amazon SQS
		try{	    	
	    	AWSCredentials auth;
	    	
	    	// If credentials provided explicitly, create new authentication object
	    	if (awsAccessKey != null && awsSecretKey != null) {
	    		auth = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
	    		
	    	// Otherwise, let SDK decide
	    	} else {
	    		auth = null;
	    	}
	    	
			AWSCredentialsProvider credentialsProvider = auth != null ? new AWSStaticCredentialsProvider(auth) : null;
			sqs = AmazonSQSClientBuilder.standard().withCredentials(credentialsProvider).withRegion(sqsRegion).build();
	    	
	    	GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(sqsQueueName);
	        sqsQueueURL = sqs.getQueueUrl(getQueueUrlRequest).getQueueUrl();
	        
			getLogger().log(Level.INFO, "SQSQueueURL is: " + sqsQueueURL);	
		} catch(Exception e){
	    	throw new ComponentNotReadyException("Unable to connect to Amazon SQS.",e);
	    }
		
		DataRecord inRecord;
		
		while ((inRecord = readRecordFromPort(0)) != null) {
			sqs.deleteMessage(new DeleteMessageRequest (sqsQueueURL,inRecord.getField(0).getValue().toString()));
		}
		
		sqs.shutdown();
	}

	@Override
	public ConfigurationStatus checkConfig(ConfigurationStatus status) {
		super.checkConfig(status);
		return status;
	}

	@Override
	public void init() {
		super.init();
		awsAccessKey = getProperties().getStringProperty(CONF_ACCESS_KEY);
		awsSecretKey = getProperties().getStringProperty(CONF_SECRET_KEY);
		sqsQueueName = getProperties().getStringProperty(CONF_QUEUE_NAME);
		sqsRegion = getProperties().getStringProperty(CONF_REGION);
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
