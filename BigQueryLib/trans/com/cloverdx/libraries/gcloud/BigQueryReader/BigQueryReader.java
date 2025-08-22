package com.cloverdx.libraries.gcloud.BigQueryReader;

import com.cloverdx.libraries.gcloud.avro.AvroValueConvertor;
import com.cloverdx.libraries.gcloud.avro.AvroValueConvertorFactory;
import com.cloverdx.libraries.gcloud.avro.MyAvroLogicalType;
import com.cloverdx.libraries.gcloud.avro.MyConversionsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.rpc.ServerStream;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.bigquery.storage.v1.*;
import org.apache.arrow.util.Preconditions;
import org.apache.avro.*;
import org.apache.avro.generic.*;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.util.Utf8;
import org.apache.log4j.Level;
import org.jetel.component.AbstractGenericTransform;
import org.jetel.data.DataRecord;
import org.jetel.exception.ComponentNotReadyException;
import org.jetel.exception.ConfigurationStatus;
import org.jetel.exception.ConfigurationStatus.Priority;
import org.jetel.exception.ConfigurationStatus.Severity;
import org.jetel.exception.JetelRuntimeException;
import org.jetel.metadata.DataFieldMetadata;
import org.jetel.metadata.DataRecordMetadata;
import org.jetel.util.formatter.TimeZoneProvider;
import org.jetel.util.string.StringUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BigQueryReader extends AbstractGenericTransform {

    private String projectId;
    private String datasetName;
    private String tableName;
    private String pathToCredentials ;
    private String rowRestriction ;
    private String selectedFields;
    private GoogleCredentials credentials;
    private BigQueryReadSettings bigQueryReadSettings;
    private DataRecord record;

    private Schema schema;
    @Override
    public void execute() {

        record = outRecords[0];

        /* Load configuration properties */
        projectId = 				getProperties().getStringProperty("PROJECT_ID");
        datasetName = 				getProperties().getStringProperty("DATASET_NAME");
        tableName = 				getProperties().getStringProperty("TABLE_NAME");
        pathToCredentials = 		getProperties().getStringProperty("PATH_TO_CREDENTIALS");
        rowRestriction = 		    getProperties().getStringProperty("ROW_RESTRICTION");
        selectedFields = 		    getProperties().getStringProperty("SELECTED_FIELDS");

        if(projectId == null || datasetName == null || tableName ==null){
            throw new JetelRuntimeException("Unable to read data, it is necessary to provide all required attributes (Project ID, Dataset name, Table name)");
        }

        String srcTable = String.format("projects/%s/datasets/%s/tables/%s", projectId, datasetName, tableName);
        String parent = String.format("projects/%s", projectId);
        getLogger().log(Level.INFO, "Initializing reading from "+srcTable);

        initCredentials();

        BigQueryReadClient client = null;
        try {
            client = BigQueryReadClient.create(this.getBigQueryReadSettings());
        } catch (IOException e) {
            throw new JetelRuntimeException("Unable to create BigQueryReadClient. "+e.getMessage());
        }
        ReadSession.TableReadOptions.Builder optionsBuilder =
                ReadSession.TableReadOptions.newBuilder();
        if(rowRestriction != null && !rowRestriction.isEmpty()){
            optionsBuilder.setRowRestriction(rowRestriction);
        }

        if(selectedFields != null && !selectedFields.isEmpty()){
            String[] fields = selectedFields.split(",");
            for (String f : fields){
                optionsBuilder.addSelectedFields(f);
            }
        }
        ReadSession.TableReadOptions options =  optionsBuilder.build();

        ReadSession.Builder sessionBuilder =
                ReadSession.newBuilder()
                        .setTable(srcTable)
                        .setDataFormat(DataFormat.AVRO)
                        .setReadOptions(options);


        CreateReadSessionRequest.Builder builder =
                CreateReadSessionRequest.newBuilder()
                        .setParent(parent)
                        .setReadSession(sessionBuilder)
                        .setMaxStreamCount(1);

        // Request the session creation.
        ReadSession session = client.createReadSession(builder.build());
        schema = new Schema.Parser().parse(session.getAvroSchema().getSchema());

        SimpleRowReader reader =
                new SimpleRowReader(new Schema.Parser().parse(session.getAvroSchema().getSchema()), this);

        // Assert that there are streams available in the session.  An empty table may not have
        // data available.  If no sessions are available for an anonymous (cached) table, consider
        // writing results of a query to a named table rather than consuming cached results directly.
        Preconditions.checkState(session.getStreamsCount() > 0);

        // Use the first stream to perform reading.
        String streamName = session.getStreams(0).getName();

        ReadRowsRequest readRowsRequest =
                ReadRowsRequest.newBuilder().setReadStream(streamName).build();

        // Process each block of rows as they arrive and decode using our simple row reader.
        ServerStream<ReadRowsResponse> stream = client.readRowsCallable().call(readRowsRequest);
        for (ReadRowsResponse response : stream) {
            Preconditions.checkState(response.hasAvroRows());
            try {
                reader.processRows(response.getAvroRows());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        client.shutdown();
        try {
            if (!client.awaitTermination(3500, TimeUnit.MILLISECONDS)) {
                client.shutdownNow();
            }
        } catch (InterruptedException e) {
            client.shutdownNow();
        }
    }

    private void initCredentials() {
        if(pathToCredentials != null && !pathToCredentials.isEmpty()){
            try (FileInputStream serviceAccountStream = new FileInputStream(pathToCredentials)) {
                this.credentials = ServiceAccountCredentials.fromStream(serviceAccountStream);
            } catch (FileNotFoundException e) {
                throw new JetelRuntimeException("Credentials file was not found on defined path: '" + pathToCredentials+"'. "+e.getMessage());
            } catch (IOException e) {
                throw new JetelRuntimeException("Credentials file was not able to load on path: '" + pathToCredentials+"'. "+e.getMessage());
            }
        }

        if(this.getCredentials() != null) {
            try {
                bigQueryReadSettings =
                        BigQueryReadSettings.newBuilder()
                                .setCredentialsProvider(FixedCredentialsProvider.create(this.getCredentials()))
                                .build();
            } catch (IOException e) {
                throw new JetelRuntimeException("Credentials file was not able to load on path: '" + pathToCredentials+"'. "+e.getMessage());
            }
        }
        else{
            try {
                bigQueryReadSettings = BigQueryReadSettings.newBuilder().build();
            } catch (IOException e) {
                throw new JetelRuntimeException("Credentials file was not able to load on path: '" + pathToCredentials+"'. "+e.getMessage());
            }
        }
    }

    @Override
    public ConfigurationStatus checkConfig(ConfigurationStatus status) {
        super.checkConfig(status);

        /* This way you can check connected edges and their metadata. */
        if (getComponent().getOutPorts().isEmpty()) {
            status.add("Output port must be connected!", Severity.ERROR, getComponent(), Priority.NORMAL);
            return status;
        }

        DataRecordMetadata outMetadata = getComponent().getOutputPort(0).getMetadata();
        if (outMetadata == null) {
            status.add("Metadata on output port not specified!", Severity.ERROR, getComponent(), Priority.NORMAL);
            return status;
        }

        return status;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void preExecute() throws ComponentNotReadyException {
        super.preExecute();
    }

    @Override
    public void postExecute() throws ComponentNotReadyException {
        super.postExecute();
    }

    public BigQueryReadSettings getBigQueryReadSettings() {
        return bigQueryReadSettings;
    }

    public GoogleCredentials getCredentials() {
        return credentials;
    }

    private AvroValueConvertor[] convertors;

    /*
     * SimpleRowReader handles deserialization of the Avro-encoded row blocks transmitted
     * from the storage API using a generic datum decoder.
     */
    private static class SimpleRowReader {
        private static final LocalDate EPOCH_DAY_0 = LocalDate.ofEpochDay(0);

        private final ZoneId DEFAULT_ZONE_ID = new TimeZoneProvider().getJavaTimeZone().toZoneId();

        private final MyConversionsProvider conversions = new MyConversionsProvider();

        private final DatumReader<GenericRecord> datumReader;

        // Decoder object will be reused to avoid re-allocation and too much garbage collection.
        private BinaryDecoder decoder = null;

        // GenericRecord object will be reused.
        private GenericRecord row = null;

        private final BigQueryReader parent;

        private AvroValueConvertor[] convertors;

        public SimpleRowReader(Schema schema, BigQueryReader parent) {
            Preconditions.checkNotNull(schema);
            this.parent = parent;
            datumReader = new GenericDatumReader<>(schema);
        }

        private void initConvertors() {
            convertors = new AvroValueConvertor[parent.record.getNumFields()];
            for (int i = 0; i < parent.record.getNumFields(); ++i) {
                convertors[i] = AvroValueConvertorFactory.getConvertorForField(parent.record.getField(i).getMetadata());
            }
        }

        /**
         * Process AVRO rows
         *
         * @param avroRows object returned from the ReadRowsResponse.
         */
        public void processRows(AvroRows avroRows) throws IOException {
            decoder =
                    DecoderFactory.get()
                            .binaryDecoder(avroRows.getSerializedBinaryRows().toByteArray(), decoder);
            initConvertors();
            Schema s = parent.schema;

            while (!decoder.isEnd()) {
                // Reusing object row
                row = datumReader.read(row, decoder);

                for(int i = 0; i< parent.record.getNumFields(); i++){
                    DataFieldMetadata metadata = parent.record.getMetadata().getField(i);
                    String fieldName = metadata.getName();

                    Schema.Field field = s.getField(fieldName);
                    if(row.hasField(fieldName)){
                        Object r = row.get(fieldName);

                        if(r != null && field != null){
                        	Object cloverDataValue = convert(r, field.schema());
                            parent.record.getField(fieldName).setValue(cloverDataValue);
                        }
                    }
                }

                parent.writeRecordToPort(0, parent.record);
                parent.record.reset();
            }
        }

        public Object convert(Object avroObject, Schema schema) {
            Object ctlObject = null;

            switch (schema.getType()) {
                case NULL:
                    ctlObject = null;
                    break;
                case STRING:
                    ctlObject = avroObject.toString();
                    break;
                case BYTES:
                    ByteBuffer buffer = (ByteBuffer) avroObject;
                    //final byte[] array = new byte[buffer. capacity()];
                    //buffer.get(array);
                    ctlObject = buffer.array();
                    break;
                case INT:
                    ctlObject = (Integer) avroObject;
                    break;
                case LONG:
                    ctlObject = (Long) avroObject;
                    break;
                case FLOAT:
                    ctlObject = Double.valueOf(((Float) avroObject).doubleValue());
                    break;
                case DOUBLE:
                    ctlObject = (Double) avroObject;
                    break;
                case BOOLEAN:
                    ctlObject = (Boolean) avroObject;
                    break;
                case FIXED:
                    GenericFixed fixedValue = (GenericFixed) avroObject;
                    ctlObject = fixedValue.bytes();
                    break;
                case ENUM:
                    GenericEnumSymbol<?> enumValue = (GenericEnumSymbol<?>) avroObject;
                    ctlObject = enumValue.toString();
                    break;
                case UNION:
                    boolean schemaTypeFound = false;
                    for (Schema typeSchema: schema.getTypes()) {
                        if(typeSchema != null) {
                            if (compareValueAndSchemaTypes(avroObject, typeSchema)) {
                                schemaTypeFound = true;
                                ctlObject = convert(avroObject, typeSchema);
                            }
                        }
                    }
                    if (!schemaTypeFound) {
                        throw new IllegalArgumentException("Avro value type '" +" "+schema.getTypes()+" / "+ avroObject.getClass().getName() + "' is not listed in union schema");
                    }
                    break;
                case ARRAY:
                    List<Object> arrayResult = new ArrayList<>();
                    GenericArray<?> arrayValue = (GenericArray<?>) avroObject;
                    for (Object arrayItem: arrayValue) {
                        arrayResult.add(convert(arrayItem, schema.getElementType()));
                    }
                    ctlObject = arrayResult;
                    break;
                case MAP:
                    Map<String, Object> mapResult = new LinkedHashMap<>();
                    Map<?, ?> avroMap = (Map<?, ?>) avroObject;
                    for (Map.Entry<?, ?> mapEntry: avroMap.entrySet()) {
                        mapResult.put(mapEntry.getKey().toString(), convert(mapEntry.getValue(), schema.getValueType()));
                    }
                    ctlObject = mapResult;
                    break;
                case RECORD:
                    Map<String, Object> recordResult = new LinkedHashMap<>();
                    GenericRecord recordValue = (GenericRecord) avroObject;
                    Schema recordSchema = recordValue.getSchema();
                    for (Schema.Field field: recordSchema.getFields()) {
                        if (field.hasProps()) {
                            field.schema().addAllProps(field);
                        }
                        Object fieldValue;
                        if (field.hasDefaultValue() && recordValue.get(field.name()) == null) {
                            if (field.defaultVal() instanceof JsonProperties.Null) {
                                fieldValue = convert(null, field.schema());
                            } else {
                                fieldValue = convert(field.defaultVal(), field.schema());
                            }
                        } else {
                            fieldValue = convert(recordValue.get(field.name()), field.schema());
                        }
                        recordResult.put(field.name(), fieldValue);
                    }
                    ctlObject = recordResult;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported type '" + schema.getType() + "'");
            }

            String logicalTypeName = schema.getProp(LogicalType.LOGICAL_TYPE_PROP);
            if (ctlObject!= null && !StringUtils.isEmpty(logicalTypeName) && !logicalTypeName.equals("datetime")  && MyAvroLogicalType.fromString(logicalTypeName) != null) {
                LogicalType logicalType = LogicalTypes.fromSchemaIgnoreInvalid(schema);
                return convertAvroLogicalTypeToJava(ctlObject, schema, logicalType);
            } else {
                return ctlObject;
            }
        }

        private Object convertAvroLogicalTypeToJava(Object avroObject, Schema schema, LogicalType logicalType) {
            if (logicalType.getName().equals(MyAvroLogicalType.DECIMAL.getTypeName()) && schema.getType() == Schema.Type.BYTES) {
                ByteBuffer buffer = ByteBuffer.wrap((byte[]) avroObject);
                return conversions.getDecimalConversion().fromBytes(buffer, schema, logicalType);
            }
            if (logicalType.getName().equals(MyAvroLogicalType.UUID.getTypeName()) && schema.getType() == Schema.Type.STRING) {
                return avroObject;
            }
            if (logicalType.getName().equals(MyAvroLogicalType.DATE.getTypeName()) && schema.getType() == Schema.Type.INT) {
                LocalDate localDate = conversions.getDateConversion().fromInt((Integer) avroObject, schema, logicalType);
                return Date.from(localDate.atStartOfDay(DEFAULT_ZONE_ID).toInstant());
            }
            if (logicalType.getName().equals(MyAvroLogicalType.TIME_MILLIS.getTypeName()) && schema.getType() == Schema.Type.INT) {
                LocalTime localTime = conversions.getTimeMillisConversion().fromInt((Integer) avroObject, schema, logicalType);
                return convertLocalTime(localTime, DEFAULT_ZONE_ID);
            }
            if (logicalType.getName().equals(MyAvroLogicalType.TIME_MICROS.getTypeName()) && schema.getType() == Schema.Type.LONG) {
                LocalTime localTime = conversions.getTimeMicrosConversion().fromLong((Long) avroObject, schema, logicalType);
                return convertLocalTime(localTime, DEFAULT_ZONE_ID);
            }
            if (logicalType.getName().equals(MyAvroLogicalType.TIMESTAMP_MILLIS.getTypeName()) && schema.getType() == Schema.Type.LONG) {
                return Date.from(conversions.getTimestampMillisConversion().fromLong((Long) avroObject, schema, logicalType));
            }
            if (logicalType.getName().equals(MyAvroLogicalType.TIMESTAMP_MICROS.getTypeName()) && schema.getType() == Schema.Type.LONG) {
                return Date.from(conversions.getTimestampMicrosConversion().fromLong((Long) avroObject, schema, logicalType));
            }
            if (logicalType.getName().equals(MyAvroLogicalType.LOCAL_TIMESTAMP_MILLIS.getTypeName()) && schema.getType() == Schema.Type.LONG) {
                LocalDateTime localDate = conversions.getLocalTimestampMillisConversion().fromLong((Long) avroObject, schema, logicalType);
                return convertLocalDateTime(localDate, DEFAULT_ZONE_ID);
            }
            if (logicalType.getName().equals(MyAvroLogicalType.LOCAL_TIMESTAMP_MICROS.getTypeName()) && schema.getType() == Schema.Type.LONG) {
                LocalDateTime localDate = conversions.getLocalTimestampMicrosConversion().fromLong((Long) avroObject, schema, logicalType);
                return convertLocalDateTime(localDate, DEFAULT_ZONE_ID);
            }
            throw new IllegalArgumentException("Unsupported logical type '" + logicalType.getName() + "' of type '" + schema.getType() + "'");
        }


        private Date convertLocalTime(LocalTime localTime, ZoneId zoneId) {
            return Date.from(localTime.atDate(EPOCH_DAY_0).atZone(zoneId).toInstant());
        }

        private Date convertLocalDateTime(LocalDateTime localDate, ZoneId zoneId) {
            return Date.from(localDate.atZone(zoneId).toInstant());
        }

        private LogicalType getLogicalType(Schema.Field field){
            LogicalType type = null;


            if(field.schema().getType().getName().toUpperCase().equals("UNION")){
                java.util.List<Schema> schemaList = field.schema().getTypes();

                for(Schema s: schemaList){
                    if(s != null && !s.getName().equalsIgnoreCase("null")){
                        if(s.getLogicalType() != null){
                            type = s.getLogicalType();
                        }
                    }
                }
            }

            return type;
        }
        public static <T> T getTypedValue(GenericRecord record, String fieldName, Class<T> clazz) {
            Object value = record.get(fieldName);
            if (value == null) {
                return null;
            }
            if (clazz.isInstance(value)) {
                return clazz.cast(value);
            }
            if (value instanceof Utf8 && clazz == String.class) {
                return clazz.cast(value.toString());
            }
            if (value instanceof ByteBuffer && clazz == byte[].class) {
                ByteBuffer buffer = (ByteBuffer) value;
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                return clazz.cast(bytes);
            }
            throw new IllegalArgumentException("Cannot convert " + value.getClass().getName() + " to " + clazz.getName());
        }

        private boolean compareValueAndSchemaTypes(Object avroObject, Schema schema) {
            switch (schema.getType()) {
                case RECORD:
                    if (avroObject instanceof GenericRecord) {
                        return true;
                    }
                    return false;
                case ENUM:
                    if (avroObject instanceof GenericEnumSymbol) {
                        return true;
                    }
                    return false;
                case ARRAY:
                    if (avroObject instanceof GenericArray) {
                        return true;
                    }
                    return false;
                case MAP:
                    if (avroObject instanceof Map) {
                        return true;
                    }
                    return false;
                case UNION:
                    //Union of unions is forbidden
                    return false;
                case FIXED:
                    if (avroObject instanceof GenericFixed) {
                        return true;
                    }
                    return false;
                case STRING:
                    if (avroObject instanceof Utf8) {
                        return true;
                    }
                    return false;
                case BYTES:
                    if (avroObject instanceof byte[] || avroObject instanceof java.nio.ByteBuffer) {
                        return true;
                    }
                    return false;
                case INT:
                    if (avroObject instanceof Integer) {
                        return true;
                    }
                    return false;
                case LONG:
                    if (avroObject instanceof Long) {
                        return true;
                    }
                    return false;
                case FLOAT:
                    if (avroObject instanceof Float) {
                        return true;
                    }
                    return false;
                case DOUBLE:
                    if (avroObject instanceof Double) {
                        return true;
                    }
                    return false;
                case BOOLEAN:
                    if (avroObject instanceof Boolean) {
                        return true;
                    }
                    return false;
                case NULL:
                    if (avroObject == null) {
                        return true;
                    }
                    return false;
                default:
            }
            return false;
        }
    }
}