package com.cloverdx.libraries.gcloud.avro;

import org.apache.avro.Schema;

public enum MyAvroLogicalType {
    DECIMAL("decimal"),
    UUID("uuid"),
    DATE("date"),
    TIME_MILLIS("time-millis"),
    TIME_MICROS("time-micros"),
    TIMESTAMP_MILLIS("timestamp-millis"),
    TIMESTAMP_MICROS("timestamp-micros"),
    LOCAL_TIMESTAMP_MILLIS("local-timestamp-millis"),
    LOCAL_TIMESTAMP_MICROS("local-timestamp-micros"),
    DURATION("duration"),
    DATETIME("datetime");

    private final String avroTypeName;

    MyAvroLogicalType(String avroTypeName) {
        this.avroTypeName = avroTypeName;
    }

    public String getTypeName() {
        return avroTypeName;
    }

    /**
     * Answers the presence of the logical type on the provided top-level schema definition.
     *
     * @param schema the immediate schema type to be checked
     * @return true if {@code schema} definition has the logical type; false otherwise
     */
    public boolean isPresent(Schema schema) {
        if (schema.getLogicalType() == null) {
            return false;
        }
        return getTypeName().equals(schema.getLogicalType().getName());
    }

    public static MyAvroLogicalType fromString(String avroTypeName) {
        for (MyAvroLogicalType type : MyAvroLogicalType.values()) {
            if (type.avroTypeName.equals(avroTypeName)) {
                return type;
            }
        }
        return null;
    }
}