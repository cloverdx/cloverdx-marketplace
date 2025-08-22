package com.cloverdx.libraries.gcloud;

import org.json.JSONArray;

public class AppendContext {

    public JSONArray data;
    int retryCount = 0;
    public JSONArray metadata;


    public AppendContext(JSONArray data, int retryCount) {
        this.data = data;
        this.retryCount = retryCount;

    }
    
    public AppendContext(JSONArray data, int retryCount, JSONArray metadata) {
        this.data = data;
        this.retryCount = retryCount;
        this.metadata = metadata;
    }
}