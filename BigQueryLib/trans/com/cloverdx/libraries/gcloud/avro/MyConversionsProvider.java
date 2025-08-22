package com.cloverdx.libraries.gcloud.avro;

import org.apache.avro.Conversions;
import org.apache.avro.data.TimeConversions;

public class MyConversionsProvider {
    private Conversions.DecimalConversion decimalConversion;
    private TimeConversions.DateConversion dateConversion;
    private TimeConversions.TimeMillisConversion timeMillisConversion;
    private TimeConversions.TimeMicrosConversion timeMicrosConversion;
    private TimeConversions.TimestampMillisConversion timestampMillisConversion;
    private TimeConversions.TimestampMicrosConversion timestampMicrosConversion;
    private TimeConversions.LocalTimestampMillisConversion localTimestampMillisConversion;
    private TimeConversions.LocalTimestampMicrosConversion localTimestampMicrosConversion;

    public Conversions.DecimalConversion getDecimalConversion() {
        if (decimalConversion == null) {
            decimalConversion = new Conversions.DecimalConversion();
        }
        return decimalConversion;
    }

    public TimeConversions.DateConversion getDateConversion() {
        if (dateConversion == null) {
            dateConversion = new TimeConversions.DateConversion();
        }
        return dateConversion;
    }

    public TimeConversions.TimeMillisConversion getTimeMillisConversion() {
        if (timeMillisConversion == null) {
            timeMillisConversion = new TimeConversions.TimeMillisConversion();
        }
        return timeMillisConversion;
    }

    public TimeConversions.TimeMicrosConversion getTimeMicrosConversion() {
        if (timeMicrosConversion == null) {
            timeMicrosConversion = new TimeConversions.TimeMicrosConversion();
        }
        return timeMicrosConversion;
    }

    public TimeConversions.TimestampMillisConversion getTimestampMillisConversion() {
        if (timestampMillisConversion == null) {
            timestampMillisConversion = new TimeConversions.TimestampMillisConversion();
        }
        return timestampMillisConversion;
    }

    public TimeConversions.TimestampMicrosConversion getTimestampMicrosConversion() {
        if (timestampMicrosConversion == null) {
            timestampMicrosConversion = new TimeConversions.TimestampMicrosConversion();
        }
        return timestampMicrosConversion;
    }

    public TimeConversions.LocalTimestampMillisConversion getLocalTimestampMillisConversion() {
        if (localTimestampMillisConversion == null) {
            localTimestampMillisConversion = new TimeConversions.LocalTimestampMillisConversion();
        }
        return localTimestampMillisConversion;
    }

    public TimeConversions.LocalTimestampMicrosConversion getLocalTimestampMicrosConversion() {
        if (localTimestampMicrosConversion == null) {
            localTimestampMicrosConversion = new TimeConversions.LocalTimestampMicrosConversion();
        }
        return localTimestampMicrosConversion;
    }
}
