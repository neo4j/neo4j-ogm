package org.neo4j.ogm.domain.convertible.date;

import org.neo4j.ogm.typeconversion.AttributeConverter;
import org.neo4j.ogm.typeconversion.DateStringConverter;

import java.util.Date;

public class DateNumericStringConverter implements AttributeConverter<Date, String> {

    private final DateStringConverter converter = new DateStringConverter("yyyyMMddhhmmss");

    @Override
    public String toGraphProperty(Date value) {
        return converter.toGraphProperty(value);
    }

    @Override
    public Date toEntityAttribute(String value) {
        return converter.toEntityAttribute(value);
    }
}
