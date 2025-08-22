package com.cloverdx.libraries.sqs.request;

import java.util.Date;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.UUID;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;

public class QueueStatusConnector extends AbstractReaderConnector<GetQueueAttributesRequest> {
	
	public QueueStatusConnector(AWSCredentials auth, String region, String queue) {
		super(auth,region,queue,DEFAULT,1,DEFAULT,DEFAULT);
	}

	@Override
	public GetQueueAttributesRequest initRequest(String queue) {
		return (new GetQueueAttributesRequest())
			.withAttributeNames("ApproximateNumberOfMessages")
			.withQueueUrl(client.getQueueUrl(new GetQueueUrlRequest(queue)).getQueueUrl());
	}

	@Override
	protected void nextBatch() throws SqsReaderRequestException {
		if (messagesLeft > 0) {
			if (buffer == null) {
				buffer = new LinkedList<Message>();
			} else {
				buffer.clear();
			}
			
			Message msg = new Message();
			msg.setMessageId(UUID.randomUUID().toString());
			msg.setBody("Amazon SQS status valid to [UNIX timestamp]: " + (new Date()).getTime());
			
			for (Entry<String,String> prop: client.getQueueAttributes(sqsRequest).getAttributes().entrySet()) {
				MessageAttributeValue attr = new MessageAttributeValue();
				attr.setDataType("string");
				attr.setStringValue(prop.getValue());
				
				msg.addMessageAttributesEntry(prop.getKey(),attr);
			}
			
			buffer.add(msg);
			cursor = buffer.iterator();
		} else {
			throw new SqsReaderRequestException("Queue attributes already retrieved. Call reset() for retry.");
		}
		
		messagesLeft--;
	}
}
