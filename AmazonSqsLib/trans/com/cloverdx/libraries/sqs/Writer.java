package com.cloverdx.libraries.sqs;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Level;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.component.RecordTransform;
import org.jetel.component.RecordTransformDescriptor;
import org.jetel.component.TransformFactory;
import org.jetel.data.DataRecord;
import org.jetel.data.RecordOrderedKey;
import org.jetel.data.reader.DriverReader;
import org.jetel.data.reader.IInputReader;
import org.jetel.data.reader.IInputReader.InputOrdering;
import org.jetel.data.reader.SlaveReaderDup;
import org.jetel.enums.OrderEnum;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.TransformException;
import org.jetel.metadata.DataRecordMetadata;
import org.jetel.util.joinKey.JoinKeyUtils;
import org.jetel.util.joinKey.OrderedKey;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * Amazon SQS writer in current version is capable to connect to amazon's SQS service
 * via native Amazon WS library. This iteration of a component enables user to
 * send custom messages with attributes, with no dependency on a incoming data structure
 * what-so-ever. This component in its most complex configuration behaves like
 * ExtMergeJoin, since it is capable of connecting messages and their attributes using
 * given sort key. It is also capable to convert any incoming attribute stream structure
 * to native Amazon SQS data format.
 * 
 * Component requires following libraries in the project:
 * + aws-java-sdk-sqs-1.11.75.jar
 * 
 * @author Pavel Svec <pavel.svec@javlin.eu>
 * @version 0.3a
 */
public class Writer extends AbstractGenericTransform {

	private final static int MASTER = 0;
	private final static int SLAVE = 1;
	/*
	 * Attribute values as assigned by output record
	 */
	private final static int ATTR_TYPE = 0;
	private final static int ATTR_NAME = 1;
	private final static int ATTR_VALUE = 2;
	private final static int ATTR_BYTE = 3;
	/*
	 * Configurable items
	 */
	private final static String CONF_BODY = "MessageBodyField";
	private final static String CONF_ACCESS_KEY = "AWSAccessKey";
	private final static String CONF_SECRET_KEY = "AWSSecretKey";
	private final static String CONF_QUEUE_NAME = "SQSQueueName";
	private final static String CONF_REGION = "SQSRegion";
	private final static String CONF_JOIN_KEY = "JoinKey";
	private final static String CONF_TRANSFORM = "Transform";
	
	DataRecord currentAttribute;
	DataRecordMetadata messageAttributeMetadata;
	RecordTransform transformation = null;
	int inPorts;
	
	String awsAccessKey = null;
	String awsSecretKey = null;
	String sqsQueueName = null;
	String sqsQueueURL = null;
	String sqsRegion = null;
	String joinKeyString = null;
	
	String transformSource;
	String transformClassName;
	String transformUrl;
	String charset;
	
	Boolean batchMode = false;
	Integer batchSize = 1;
	
	OrderedKey[][] joinKeys;
	IInputReader[] reader;
	RecordOrderedKey[] recordKeys;
	
	int bodyFieldPosition;
	
	AmazonSQS	sqs = null;
	ComponentNotReadyException initException;
	
	@Override
	public void execute() throws ComponentNotReadyException, IOException, InterruptedException, TransformException {
		if (initException != null) {
			throw initException;
		}
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
		// Create skeleton of message request 
		SendMessageRequest req = (new SendMessageRequest())
			.withQueueUrl(sqsQueueURL);
		
		// When both ports are connected, message is going
		// to be sent with attributes (joiner algorithm needs to kick in)
		if (inPorts > 1) {
			sendWithAttributes(req);
		
		// Send only using master input port
		// ...simply read input until there is nothing to read
		} else {
			sentWithoutAttributes(req);
		}
		
		sqs.shutdown();
	}
	
	/**
	 * Sends simple messages when attribute input is not connected, read only master message 
	 * every incoming record is sent via new message.
	 * @param request
	 * @return
	 */
	private boolean sentWithoutAttributes(SendMessageRequest request) {
		DataRecord inRecord;
		
		while ((inRecord = readRecordFromPort(MASTER)) != null) {
			request.setMessageBody(inRecord.getField(bodyFieldPosition).getValue().toString());
			sqs.sendMessage(request);
		}
		
		return true;
	}
	
	private boolean sendWithAttributes(SendMessageRequest request) throws InterruptedException, IOException, ComponentNotReadyException, TransformException {
		Object value;
		
		// Get and initialize join keys
		joinKeys = JoinKeyUtils.parseMergeJoinOrderedKey(joinKeyString, getComponent().getInMetadata());
				
		if (joinKeys.length < inPorts) {
			getLogger().warn("Join keys aren't specified for all slave inputs - deducing missing keys");
			OrderedKey[][] replJoiners = new OrderedKey[inPorts][];
					
			for (int i = 0; i < joinKeys.length; i++) {
				replJoiners[i] = joinKeys[i];
		    }
		    		
		 	// use driver key list for all missing slave key specifications
   			for (int i = joinKeys.length; i < inPorts; i++) {
   				replJoiners[i] = joinKeys[MASTER];
   			}
		    		
   			joinKeys = replJoiners;
		}
		
		recordKeys[MASTER] = buildRecordKey(joinKeys[MASTER], getComponent().getInMetadata().get(MASTER));
		recordKeys[SLAVE] = buildRecordKey(joinKeys[SLAVE], getComponent().getInMetadata().get(SLAVE));
		
		/* DEPRECATED FUNCTION CALL
		for (RecordOrderedKey key: recordKeys) {
			key.init();
		}
		*/
		
		// Initialize input readers
		reader[MASTER] = new DriverReader(getComponent().getInputPort(MASTER), recordKeys[MASTER]);
		reader[SLAVE] = new SlaveReaderDup(getComponent().getInputPort(SLAVE), recordKeys[SLAVE], false);
		
		DataRecord[] input = new DataRecord[1];
		DataRecord[] output = outRecords;
		
		// Get new master record
		loadNextRun(reader[SLAVE],SLAVE);
		// Until there are data on master input port
		while (loadNextRun(reader[MASTER],MASTER)) {
			int compare = 1;

			// Skip anything, which cannot be matched by master record
			while (reader[SLAVE].getSample() != null && (compare = reader[MASTER].compare(reader[SLAVE])) > 0)
				loadNextRun(reader[SLAVE],SLAVE);
			
			// Add matching attributes to the message
			// When there
			while ((inRecords[0] = reader[MASTER].next()) != null) {
				request.setMessageBody(inRecords[0].getField(bodyFieldPosition).getValue().toString());
			
				if (compare == 0) {
					while ((input[0] = reader[SLAVE].next()) != null) {
						int transformResult = -1;

						try {
							transformResult = transformation.transform(input, output);
						} catch (Exception exception) {
							transformResult = transformation.transformOnError(exception, input, output);
						}
						
						// OK and ALL consider as valid
						if (transformResult == RecordTransform.ALL || transformResult == RecordTransform.OK) {
							String dataType = output[0].getField(ATTR_TYPE).getValue().toString();
							MessageAttributeValue mav = new MessageAttributeValue();
							mav.setDataType(dataType);
					
							if (dataType.toLowerCase().startsWith("binary")) {
								value = output[0].getField(ATTR_BYTE).getValue();
								mav.setBinaryValue(ByteBuffer.wrap((byte[]) value));
							} else {
								value = output[0].getField(ATTR_VALUE).getValue();
								mav.setStringValue(value.toString());
							}
						
							request.addMessageAttributesEntry(output[0].getField(ATTR_NAME).getValue().toString(),mav);
						}
						
						// For stop action - just stop processing of attributes for this particular message
						if (transformResult == RecordTransform.STOP)
							break;
					}
				}
				
				// Send message and reset attributes
				sqs.sendMessage(request);
				request.clearMessageAttributesEntries();
				// And rewind attributes list
				reader[SLAVE].rewindRun();
			}
		}
		
		// Release records from slave port
		while (reader[SLAVE].hasData()) {
			reader[SLAVE].loadNextRun();
		}
		
		return true;
	}
	
	/**
	 * Wrapper for native data reader. Adds exceptions, when data in incorrect order are received.
	 * @param ir
	 * @param inPort
	 * @return
	 * @throws IllegalStateException
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private boolean loadNextRun(IInputReader ir, int inPort) throws IllegalStateException, InterruptedException, IOException {
		boolean result = ir.loadNextRun();
		
		// check inputs ordering 
		if (ir.getOrdering() == InputOrdering.UNSORTED) 		
			throw new IllegalStateException("Data input "+inPort+" is not sorted in ascending order. "+ir.getInfo());
		if (ir.getOrdering() == InputOrdering.DESCENDING)
			throw new IllegalStateException("input " + inPort + " has wrong ordering; change ordering on field or ordering of input "+inPort);
		
		return result;
	}
	
	/**
	 * Constructs a RecordComparator based on particular metadata and settings
	 * 
	 * @param metaData
	 * @return
	 * @throws ComponentNotReadyException 
	 */
	private RecordOrderedKey buildRecordKey(OrderedKey joiners[], DataRecordMetadata metaData) throws ComponentNotReadyException {
		int[] fields = new int[joiners.length];
		boolean[] aOrdering = new boolean[joiners.length];
		
		// For all fields, participating on a key
		for (int i = 0; i < fields.length; i++) {
			fields[i] = metaData.getFieldPosition(joiners[i].getKeyName());
			// When ordering is defined in a key
			if (joiners[i].getOrdering() == OrderEnum.ASC) {
				aOrdering[i] = true;
				
			// When ordering is not defined at all
			} else if (joiners[i].getOrdering() == null) {	
				boolean isAsc;
				
				if (recordKeys[MASTER] != null && recordKeys[MASTER].getKeyOrderings().length > i) {
					// set the same ordering as the master has
					isAsc = recordKeys[MASTER].getKeyOrderings()[i];
					
				} else {
					// default to ascending order
					isAsc = true;
				}
				
				joiners[i].setOrdering(isAsc ? OrderEnum.ASC : OrderEnum.DESC);
				aOrdering[i] = isAsc;
				
			// Not a recognized ordering? Terminate...
			} else if (joiners[i].getOrdering() != OrderEnum.DESC) {
				throw new ComponentNotReadyException("Wrong order definition in join key: " + joiners[i].getOrdering());
			}
		}
		
		return new RecordOrderedKey(fields, aOrdering, metaData);
	}

	@Override
	public ConfigurationStatus checkConfig(ConfigurationStatus status) {
		super.checkConfig(status);
		int inPorts = getComponent().getInPorts().size();
		
		/*
		 * Body attribute validation
		 */
		String bodyAttribute = getProperties().getProperty(CONF_BODY);
		if (bodyAttribute == null || bodyAttribute.isEmpty()) {
			status.addWarning(getComponent(),"Body attribute","Message body field is not configured.");
		} else if (getComponent().getInMetadata().get(0).getFieldPosition(bodyAttribute) < 0) {
			status.addError(getComponent(),"Body attribute","Message body field '"+bodyAttribute+"' does not exist in '"+getComponent().getInMetadata().get(0).getName()+"'.");
		}
		/*
		 * Queue name validation
		 */
		String queueName = getProperties().getProperty(CONF_QUEUE_NAME);
		if (queueName == null || queueName.isEmpty()) {
			status.addWarning(getComponent(),"Queue name","SQS queue name is missing.");
		}
		

		if (inPorts < 1) {
			status.addError(getComponent(),"Input port 0","At least one edge needs to be connected to a component.");
			return status;
		}
		
		/**
		 * This is currently being validated by other components placed on the subgraph, along with
		 * this writer class. Section below would be just duplicate of existing functionality of a subgraph.
		 */
		/*
		if (inPorts > 1) {
			
			 * Join key definition validation
			
			String joinKey = getProperties().getProperty(CONF_JOIN_KEY);
			if (queueName == null || queueName.isEmpty()) {
				status.add("Join key is required when attribute input port is connected.", Severity.ERROR, getComponent(), Priority.NORMAL);
			} else {
				// Get and initialize join keys
				try {
					OrderedKey[][] keys = JoinKeyUtils.parseMergeJoinOrderedKey(joinKey, getComponent().getInMetadata());
					if (keys.length < inPorts) {
						status.add("Join key is not defined for all input ports.", Severity.WARNING, getComponent(), Priority.NORMAL);
					}
				} catch (ComponentNotReadyException e) {
					status.add("Inserted join key is invalid: " + e.getMessage(), Severity.ERROR, getComponent(), Priority.NORMAL);
				}
			}
			
			transformSource = getProperties().getProperty(CONF_TRANSFORM);
			getTransformFactory().checkConfig(status);
		}
		*/
		return status;
	}

	@Override
	public void init() {
		super.init();
		String messageBodyField = getProperties().getStringProperty(CONF_BODY);
		awsAccessKey = getProperties().getStringProperty(CONF_ACCESS_KEY);
		awsSecretKey = getProperties().getStringProperty(CONF_SECRET_KEY);
		sqsQueueName = getProperties().getStringProperty(CONF_QUEUE_NAME);
		sqsRegion = getProperties().getStringProperty(CONF_REGION);
		inPorts = getComponent().getInPorts().size();
		
		// Find message body field position from record
		bodyFieldPosition = getComponent().getInMetadata().get(0).getFieldPosition(messageBodyField);
		
		// Attribute port does not have to be connected
		if (inPorts > 1) {
			joinKeyString = getProperties().getProperty(CONF_JOIN_KEY);
			transformSource = getProperties().getProperty(CONF_TRANSFORM);
			transformClassName = getProperties().getProperty("TransformClassName");
			transformUrl = getProperties().getProperty("TransformUrl");
			
			if (transformation == null) {
				transformation = getTransformFactory().createTransform();
	        }
	        
			// init transformation
			try {
				DataRecordMetadata[] srcMeta = new DataRecordMetadata[] { getComponent().getInMetadata().get(1) };
				DataRecordMetadata[] tgtMeta = new DataRecordMetadata[] { getComponent().getOutMetadata().get(0) };
				
				if (!transformation.init(null , srcMeta, tgtMeta)) {
					initException = new ComponentNotReadyException("Error when initializing tranformation function.");
				}
			} catch (ComponentNotReadyException e) {
				initException = e;
			}
			
			reader = new IInputReader[inPorts];
			recordKeys = new RecordOrderedKey[inPorts];
		}
	}

	@Override
	public void preExecute() throws ComponentNotReadyException {
		super.preExecute();
		// There is no transformation when attributes are not connected
		if (transformation != null)
			transformation.preExecute();
	}

	@Override
	public void postExecute() throws ComponentNotReadyException {
    	super.postExecute();
		// There is no transformation when attributes are not connected
		if (transformation != null)
			transformation.postExecute();
	}
	
	/**
	 * Creates transformation engine for mapping attribute values to SQS data format
	 * @param inMetadata
	 * @param outMetadata
	 * @return
	 */
	private TransformFactory<RecordTransform> getTransformFactory() {
		DataRecordMetadata[] srcMeta = new DataRecordMetadata[] { getComponent().getInMetadata().get(1) };
		DataRecordMetadata[] tgtMeta = new DataRecordMetadata[] { getComponent().getOutMetadata().get(0) };
		
    	TransformFactory<RecordTransform> transformFactory = TransformFactory.createTransformFactory(RecordTransformDescriptor.newInstance());
    	transformFactory.setTransform(transformSource);
    	transformFactory.setTransformClass(transformClassName);
    	transformFactory.setTransformUrl(transformUrl);
    	transformFactory.setCharset(charset);
    	transformFactory.setComponent(getComponent());
    	transformFactory.setInMetadata(srcMeta);
    	transformFactory.setOutMetadata(tgtMeta);
    	return transformFactory;
	}
}
