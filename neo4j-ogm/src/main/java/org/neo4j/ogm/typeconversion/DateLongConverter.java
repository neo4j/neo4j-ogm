package org.neo4j.ogm.typeconversion;

import java.util.Date;

public class DateLongConverter implements AttributeConverter<Date, Long> {

    @Override
    public Long toGraphProperty(Date value) {
        if (value == null) return null;
        return value.getTime();
    }

    @Override
    public Date toEntityAttribute(Long value) {
        if (value == null) return null;
        return new Date(value);
    }
}
