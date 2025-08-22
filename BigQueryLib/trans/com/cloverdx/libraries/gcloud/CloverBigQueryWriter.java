package com.cloverdx.libraries.gcloud;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.cloverdx.libraries.gcloud.BigQueryBulkWriter.BigQueryWriter;
import com.google.cloud.bigquery.JobException;
import com.google.cloud.bigquery.storage.v1.BigQueryWriteClient;
import com.google.cloud.bigquery.storage.v1.TableName;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.DescriptorValidationException;

public abstract class CloverBigQueryWriter {
	

	public void initialize(TableName parentTable, String pathToCredentials, boolean ignoreUnknownFields, int maxRetryCount, int maxRecreateCount) throws Descriptors.DescriptorValidationException, IOException, InterruptedException {
	}
	
	public void initialize(TableName parentTable, BigQueryWriteClient client, String pathToCredentials, String processingType, boolean ignoreUnknownFields, int maxRetryCount, int maxRecreateCount) throws DescriptorValidationException, IOException, InterruptedException {
	}
	
	public void initialize(TableName parentTable, BigQueryWriteClient client, String pathToCredentials, boolean ignoreUnknownFields, int maxRetryCount, int maxRecreateCount) throws DescriptorValidationException, IOException, InterruptedException {
	}

	public void setErrorHandlerClass(BigQueryWriter bigQueryWriter) {
	}

	public void append(AppendContext appendContext)  throws DescriptorValidationException, IOException, InterruptedException{
	}

	public void cleanup() {
	}

	public void append(AppendContext appendContext, long offset) throws DescriptorValidationException, IOException, ExecutionException{
		
	}

	public void cleanup(BigQueryWriteClient client) {
	}
	
	public abstract String getStreamName() ;
	
	public abstract int getNumRows() throws JobException, InterruptedException;
	
	public abstract long truncate() throws JobException, InterruptedException;

}
