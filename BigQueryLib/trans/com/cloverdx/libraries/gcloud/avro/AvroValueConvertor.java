package com.cloverdx.libraries.gcloud.avro;

import org.apache.avro.LogicalType;

public interface AvroValueConvertor {
    public Object convert(Object value, LogicalType type);
}
