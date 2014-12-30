package org.neo4j.ogm.typeconversion;

import java.util.Date;

public class DateLongConverter implements AttributeConverter<Date, Long> {

    @Override
    public Long toGraphProperty(Object value) {
        return ((Date)value).getTime();
    }

    @Override
    public Date toEntityAttribute(Object value) {
        return new Date((Long) value);
    }
}
