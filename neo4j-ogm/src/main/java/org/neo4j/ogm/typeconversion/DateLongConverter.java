package org.neo4j.ogm.typeconversion;

import java.util.Date;

public class DateLongConverter implements AttributeConverter<Date, Long> {

    @Override
    public Long toGraphProperty(Date value) {
        return value.getTime();
    }

    @Override
    public Date toEntityAttribute(Long value) {
        return new Date(value);
    }
}
