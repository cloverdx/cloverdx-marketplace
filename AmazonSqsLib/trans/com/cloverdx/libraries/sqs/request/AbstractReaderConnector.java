package com.cloverdx.libraries.sqs.request;

import java.util.Iterator;
import java.util.List;

import com.amazonaws.AmazonWebServiceRequest;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;

abstract public class AbstractReaderConnector<T extends AmazonWebServiceRequest> implements ISqsConnector {

	public final static Integer BATCH_SIZE = 10;
	public final static int DEFAULT = -1;
	
	protected int batchSize;
	protected int maxSize;
	protected int messagesLeft;
	protected int waitTime;
	protected int visibilityTimeout;
	protected List<Message> buffer;
	
	protected AmazonSQS client;
	protected Iterator<Message> cursor;
	protected T sqsRequest;
	
	public AbstractReaderConnector(AWSCredentials auth, String region, String queue, int batchSize, int maxSize, int waitTime, int visibilityTimeout) {
		// 
		// When authentication is provided, use it; otherwise use default SDK deal with
		// the situation as per: https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html
		// https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html
		
		AWSCredentialsProvider credentialsProvider = auth != null ? new AWSStaticCredentialsProvider(auth) : null;
		client = AmazonSQSClientBuilder.standard().withCredentials(credentialsProvider).withRegion(region).build();
		
		this.batchSize = batchSize;
		this.maxSize = maxSize;
		this.waitTime = waitTime;
		this.visibilityTimeout = visibilityTimeout;
		this.sqsRequest = initRequest(queue);
		reset();
	}
	
	public void reset() {
		messagesLeft = maxSize;
	}
	
	public void close() {
		client.shutdown();
	}
	
	@Override
	public Iterator<Message> iterator() {
		return new SqsMessageIterator();
	}
	
	abstract public T initRequest(String queue);
	abstract protected void nextBatch() throws SqsReaderRequestException;
	
	/**
	 * Custom iterator, makes new SQS request when it hits to end of list 
	 */
	protected class SqsMessageIterator implements Iterator<Message> {
		@Override
		public boolean hasNext() {
			if (buffer == null || !cursor.hasNext()) {
				try {
					nextBatch();
				// 
				// Unable to read next batch; either quota exhausted or
				// no more messages in the queue
				} catch (SqsReaderRequestException ex) {
					return false;
				}
			}
			
			return true;
		}

		@Override
		public Message next() {
			return cursor.next();
		}
	}
}
