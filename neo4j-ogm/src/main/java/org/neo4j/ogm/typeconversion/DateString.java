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
public class DateString implements AttributeConverter<Date, String> {

    private String format;

    public DateString(String userDefinedFormat) {
        this.format = userDefinedFormat;
    }

    @Override
    public String toGraphProperty(Date value) {
        return new SimpleDateFormat(format).format(value);
    }

    @Override
    public Date toEntityAttribute(String value) {
        try {
            return new SimpleDateFormat(format).parse(value);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }


}
