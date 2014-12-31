package org.neo4j.ogm.domain.convertible.date;

import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.DateStringConverter;

import java.util.Date;

public class DateNumericStringConverter implements AttributeConverter<Date, String> {

    private final DateStringConverter converter = new DateStringConverter("yyyyMMddhhmmss");

    @Override
    public <F> F toGraphProperty(Object value) {
        return (F) converter.toGraphProperty(value);
    }

    @Override
    public <T> T toEntityAttribute(Object value) {
        return (T) converter.toEntityAttribute(value);
    }
}
