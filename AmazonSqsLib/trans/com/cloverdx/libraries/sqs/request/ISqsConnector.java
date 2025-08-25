package com.cloverdx.libraries.sqs.request;

import com.amazonaws.services.sqs.model.Message;

public interface ISqsConnector extends Iterable<Message> {
	public void close();
}
