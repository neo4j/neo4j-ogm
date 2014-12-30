package org.neo4j.ogm.typeconversion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * By default the OGM will map date objects to ISO8601 compliant
 * String values when being stored as a node / relationship property
 *
 * Users can override this behaviour for Date objects using
 * the appropriate annotations:
 *
 * @DateString("format") will convert between dates and strings
 * using a user defined date format, e.g. "yy-MM-dd"
 *
 * @DateLong will read and write dates as Long values in the database.
 */
public class DateStringConverter implements AttributeConverter<Date, String> {

    private String format;

    public DateStringConverter(String userDefinedFormat) {
        this.format = userDefinedFormat;
    }

    @Override
    public String toGraphProperty(Object value) {
        return new SimpleDateFormat(format).format((Date) value);
    }

    @Override
    public Date toEntityAttribute(Object value) {
        try {
            return new SimpleDateFormat(format).parse((String) value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
