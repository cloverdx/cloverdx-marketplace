package com.cloverdx.libraries.sqs.request;

import java.util.Iterator;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class PendingMessageConnector extends AbstractReaderConnector<ReceiveMessageRequest> {

	// Delete messages automatically after reading
	private boolean autoAck = false;
	private int batch_counter = 0;
	
	public PendingMessageConnector(AWSCredentials auth, String region, String queue, int batchSize, int maxSize, int waitTime, int visibilityTimeout) {
		super(auth,region,queue,batchSize,maxSize, waitTime, visibilityTimeout);
	}
	
	public void setAutoAck() {
		autoAck = true;
	}
	
	public void unsetAutoAck() {
		autoAck = false;
	}
	
	public boolean getAutoAck() {
		return autoAck;
	}
	
	@Override
	public ReceiveMessageRequest initRequest(String queue) {
		ReceiveMessageRequest tmp = (new ReceiveMessageRequest())
				.withMessageAttributeNames(".*")
				.withQueueUrl(client.getQueueUrl(new GetQueueUrlRequest(queue)).getQueueUrl());
		if (waitTime >= 0) {
			tmp = tmp.withWaitTimeSeconds(waitTime);
		}
		if (visibilityTimeout > 0) {
			tmp = tmp.withVisibilityTimeout(visibilityTimeout);
		}
		return tmp;
	}
	
	@Override
	protected void nextBatch() throws SqsReaderRequestException {
		
		//pull wait time to 0 on subsequent requests
		if (batch_counter == 1) {
				sqsRequest.setWaitTimeSeconds(0);
		}
		
		// Get size of next request
		int requestSize = maxSize < 1 ? batchSize : Math.min(maxSize, Math.min(batchSize,messagesLeft));
		
		if (requestSize > 0) {
			sqsRequest.setMaxNumberOfMessages(requestSize);
			buffer = client.receiveMessage(sqsRequest).getMessages();
			cursor = buffer.iterator();
			messagesLeft -= buffer.size();
		} else {
			buffer = null;
		}
		
		if (buffer == null || buffer.size() == 0) {
			throw new SqsReaderRequestException("No more messages to read.");
		}
		
		batch_counter++;
	}
	
	@Override
	public Iterator<Message> iterator() {
		return new AckSqsMessageIterator();
	}
	
	private class AckSqsMessageIterator extends SqsMessageIterator {
		
		@Override
		public Message next() {
			Message m = super.next();
			
			if (autoAck && m != null) {
				client.deleteMessage(new DeleteMessageRequest(sqsRequest.getQueueUrl(),m.getReceiptHandle()));
			}
			
			return m;
		}
		
	}
}
