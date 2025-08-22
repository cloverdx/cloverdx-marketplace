package com.cloverdx.libraries.gcloud.avro;

import java.nio.ByteBuffer;
import java.util.Date;

import org.apache.avro.LogicalType;
import org.apache.avro.util.Utf8;
import org.jetel.data.primitive.*;
import org.jetel.exception.JetelRuntimeException;
import org.jetel.metadata.DataFieldMetadata;
import org.jetel.metadata.DataFieldType;
import org.jetel.util.bytes.CloverBuffer;
import org.jetel.util.string.CloverString;

public class AvroValueConvertorFactory {
    public static String mapCloverTypeToAvscType(DataFieldType type) {
        switch (type) {
            case BOOLEAN:
                return "boolean";

            case BYTE:
            case CBYTE:
                return "bytes";

            case DATE:
                return "long"; // TODO use logical type: time-millis

            case DECIMAL:
                return "string"; // TODO use logical type: decimal

            case INTEGER:
                return "int";

            case LONG:
                return "long";

            case NUMBER:
                return "double";

            case STRING:
                return "string";

            default:
                throw new JetelRuntimeException("Unexpected field type \"" + type + "\".");
        }
    }

    public static AvroValueConvertor getConvertorForField(DataFieldMetadata field) {
        switch (field.getDataType()) {
            case BOOLEAN:
                return new AvroIntConvertor();
            case INTEGER:
            case LONG:
                return new AvroIntConvertor();
            case NUMBER:
                return new AvroDirectConvertor();
            case BYTE:
            case CBYTE:
                return new AvroByteArrayConvertor();
            case DATE:
                return new AvroDateConvertor();
            case DECIMAL:
                return new AvroDecimalConvertor();
            case STRING:
                return new AvroStringConvertor();
            default:
                throw new JetelRuntimeException("Unexpected field type \"" + field.getDataType() + "\" for field \"" + field.getName() + "\".");
        }
    }

    private static class AvroIntConvertor implements AvroValueConvertor {
        @Override
        public Object convert(Object value, LogicalType logicalType) {
            return value;
        }
    }

    private static class AvroByteArrayConvertor implements AvroValueConvertor {
        @Override
        public byte[] convert(Object value, LogicalType logicalType) {
            java.nio.ByteBuffer hbb = (ByteBuffer) value;
            return hbb.array();
        }
    }
    private static class AvroDirectConvertor implements AvroValueConvertor {
        @Override
        public Object convert(Object value, LogicalType logicalType) {
            return value;
        }
    }

    private static class AvroStringConvertor implements AvroValueConvertor {
        @Override
        public Object convert(Object value, LogicalType logicalType) {
            if (value == null) {
                return null;
            }
            if (value instanceof Utf8) {
                return ((Utf8) value).toString();
            }
            else {
                // At this point we will have instance of CloverString.
                return new CloverString(value.toString());
            }
        }
    }

    private static class AvroDateConvertor implements AvroValueConvertor {
        @Override
        public Object convert(Object value, LogicalType logicalType) {

            if (value == null) {
                return null;
            }

            if(logicalType != null && logicalType.getName() == "time-mi")

            if(value instanceof java.lang.Long){
                return new Date((Long) value);
            }

            // At this point we have instance of Date. We stores dates as Unix timestamp with ms precision.
            return (Long) ((Date) value).getTime();
        }
    }

    private static class AvroDecimalConvertor implements AvroValueConvertor {
        @Override
        public Object convert(Object value, LogicalType logicalType) {

            if(value instanceof java.nio.ByteBuffer){
                ByteBuffer v = (ByteBuffer) value;
                CloverBuffer cloverBuffer =  CloverBuffer.wrap(v);

                HugeDecimal dec = new HugeDecimal (12, 10);
                dec.deserialize(cloverBuffer);

                return dec;
            }

            if (value == null) {
                return null;
            }

            return ((Numeric) value).toString();
        }
    }
}