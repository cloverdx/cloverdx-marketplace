package com.cloverdx.libraries.gcloud;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.jetel.data.BooleanDataField;
import org.jetel.data.DataField;
import org.jetel.data.DateDataField;
import org.jetel.data.StringDataField;
import org.jetel.data.primitive.HugeDecimal;
import org.jetel.exception.JetelRuntimeException;
import org.jetel.metadata.DataFieldMetadata;
import org.jetel.metadata.DataFieldType;
import org.jetel.util.string.CloverString;

import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.StandardSQLTypeName;
import com.google.protobuf.ByteString;

public class GoogleCloudUtils {
	
	//Boolean, Double, Integer, JSONArray, JSONObject, Long, String, or the JSONObject.NULL object.
	public static Object convertCloverDataTypeToJSON(DataFieldMetadata metadata, Object value) throws JetelRuntimeException{
		DataFieldType type = metadata.getDataType();
		if(type == null) {
			throw new JetelRuntimeException("Unknown data type");
		}
		
		if(value == null) return null;

		String cloverDataType =  type.getName().toUpperCase();
        switch (cloverDataType) {
            case "STRING":
                return CloverString.stringValue((CloverString) value);
// Get date format from metadata
            case "DATE":
                String dateFormat = metadata.getFormat();
                if (dateFormat == null) {
                    dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
                }
                SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
                return formatter.format(value);
            case "NUMBER":
                return (Double) value;
            case "INTEGER":
                return (Integer) value;
            case "LONG":
                return (Long) value;
            case "DECIMAL":
                HugeDecimal val = (HugeDecimal) value;
                return val.getBigDecimal().toString();
            case "BYTE":
            case "CBYTE":
                return ByteString.copyFrom((byte[]) value);
            case "BOOLEAN":
                return (Boolean) value;
            case "VARIANT":
                throw new JetelRuntimeException("Variant data type is not supported");
            case "LIST":
                throw new JetelRuntimeException("List data type is not supported");
            case "MAP":
                throw new JetelRuntimeException("Map data type is not supported");
            default:
                return (String) value;
        }
	}
	
	public static DataField jsonObjectToCloverDataField(DataField targetDataField, Object obj) {
		DataFieldType type = targetDataField.getMetadata().getDataType();
		DataField out;
        /*
			case "NUMBER": 	return (Double) value;
			case "INTEGER": return (Integer) value;
			case "LONG": 	return (Long) value;
			case "DECIMAL": return (String) value;
			case "BYTE": 	return ByteString.copyFrom((byte[])value);
			case "CBYTE": 	return ByteString.copyFrom((byte[])value);
			*/
        /*
			case "VARIANT": throw new JetelRuntimeException("Variant data type is not supported");
			case "LIST": 	throw new JetelRuntimeException("List data type is not supported");
			case "MAP": 	throw new JetelRuntimeException("Map data type is not supported");
			*/
        switch (type.getName().toUpperCase()) {
            case "STRING":
                out = new StringDataField(targetDataField.getMetadata(), (String) obj);
                break;
// Get date format from metadata
            case "DATE":
                String dateFormat = targetDataField.getMetadata().getFormat();
                if (dateFormat == null) {
                    dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
                }
                SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
                Date targetDate;
                try {
                    targetDate = formatter.parse((String) obj);
                } catch (ParseException e) {
                    targetDate = null;
                    System.out.println("Unable to parse date : " + obj + " using format: " + dateFormat);
                }
                out = new DateDataField(targetDataField.getMetadata(), targetDate);
                break;
            case "BOOLEAN":
                out = new BooleanDataField(targetDataField.getMetadata());
                out.setValue(obj);
                break;
            default:
                out = null;
                break;
        }
		return out;
	}

    public static DataFieldType avroTypeToCloverDataField(String typeName) {
        DataFieldType type ;
        switch (typeName.toUpperCase()) {
            case "INTEGER":
                type = DataFieldType.INTEGER;
                break;
            case "LONG":
                type = DataFieldType.LONG;
                break;
            case "DOUBLE":
                type = DataFieldType.NUMBER;
                break;
            case "BYTES":
                type = DataFieldType.BYTE;
                break;
            case "STRING":
                type = DataFieldType.STRING;
                break;
            case "TIME-MICROS":
            case "TIMESTAMP-MICROS":
            case "DATE":
                 type = DataFieldType.DATE;
                break;
            case "BOOLEAN":
                type = DataFieldType.BOOLEAN;
                break;
            case "DECIMAL":
                type = DataFieldType.DECIMAL;
                break;
            case "DATETIME":
            default:
                type = DataFieldType.STRING;
                break;
        }
        return type;
    }

	public static StandardSQLTypeName convertCloverDataTypeToSQL(DataFieldMetadata fieldMetadata) {
		DataFieldType type = fieldMetadata.getDataType();
		
		if(type == null) {
			throw new JetelRuntimeException("Unknown data type");
		}

		String cloverDataType =  type.getName().toUpperCase();
        switch (cloverDataType) {
            case "DATE":
                if (fieldMetadata.getFormat().equals("HH:mm:ss")) {
                    return StandardSQLTypeName.TIME;
                } else if (fieldMetadata.getFormat().equals("yyyy-MM-dd HH:mm:ss.SSSz")) {
                    return StandardSQLTypeName.TIMESTAMP;
                } else if (fieldMetadata.getFormat().equals("yyyy-MM-dd HH:mm:ss")) {
                    return StandardSQLTypeName.DATETIME;
                } else {
                    return StandardSQLTypeName.DATETIME;
                }
            case "NUMBER":
            case "DECIMAL":
                return StandardSQLTypeName.NUMERIC;
            case "INTEGER":
            case "LONG":
                return StandardSQLTypeName.INT64;
            case "BYTE":
            case "CBYTE":
                return StandardSQLTypeName.BYTES;
            case "BOOLEAN":
                return StandardSQLTypeName.BOOL;
            case "VARIANT":
                throw new JetelRuntimeException("Variant data type is not supported");
            case "LIST":
                throw new JetelRuntimeException("List data type is not supported");
            case "MAP":
                throw new JetelRuntimeException("Map data type is not supported");
            default:
                return StandardSQLTypeName.STRING;
        }
	}

	public static DataFieldType legacySQLTypeNameToCloverDataField(LegacySQLTypeName legacyType) {
        DataFieldType type ;
		switch (legacyType.name().toUpperCase()) {
            case "INTEGER":
                type = DataFieldType.INTEGER;
                break;
            case "LONG":
                type = DataFieldType.LONG;
                break;
            case "BIGNUMERIC":
            case "FLOAT":
            case "NUMERIC":
            case "DOUBLE":
                type = DataFieldType.NUMBER;
                break;
            case "BYTES":
                type = DataFieldType.BYTE;
                break;
            case "STRING":
                type = DataFieldType.STRING;
                break;
            case "DATE":
            case "TIME":
            case "TIMESTAMP":
                 type = DataFieldType.DATE;
                break;
            case "BOOLEAN":
                type = DataFieldType.BOOLEAN;
                break;
            case "DECIMAL":
                type = DataFieldType.DECIMAL;
                break;
            case "GEOGRAPHY":
            case "INTERVAL":
            case "JSON":
            case "DATETIME":
            default:
                type = DataFieldType.STRING;
                break;
        }
        return type;
	}
}