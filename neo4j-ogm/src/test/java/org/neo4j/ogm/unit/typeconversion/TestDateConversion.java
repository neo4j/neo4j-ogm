package org.neo4j.ogm.unit.typeconversion;

import org.junit.Test;

import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.typeconversion.*;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestDateConversion {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.date");
    private static final ClassInfo memoInfo = metaData.classInfo("Memo");

    @Test
    public void assertDateConversionToISO8601FormatByDefault() {
        FieldInfo fieldInfo = memoInfo.propertyField("recorded");
        assertTrue(fieldInfo.isConvertible());
        AttributeConverter<?, ?> attributeConverter = fieldInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class));
        Date date = new Date(0);
        String value = attributeConverter.toGraphProperty(date);
        assertEquals("1970-01-01T00:00:00.000Z", value);
    }

    @Test
    public void assertDateConversionWithUserDefinedFormat() {
        FieldInfo fieldInfo = memoInfo.propertyField("actioned");
        assertTrue(fieldInfo.isConvertible());
        AttributeConverter<?, ?> attributeConverter = fieldInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class));
        Date date = new Date(0);
        String value = attributeConverter.toGraphProperty(date);
        assertEquals("1970-01-01", value);
    }

    @Test
    public void assertDateLongConversion() {
        FieldInfo fieldInfo = memoInfo.propertyField("closed");
        assertTrue(fieldInfo.isConvertible());
        AttributeConverter<?, ?> attributeConverter = fieldInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateLongConverter.class));
        Date date = new Date(0);
        Long value = attributeConverter.toGraphProperty(date);
        assertEquals(new Long(0), value);
    }

    @Test
    public void assertCustomTypeConversion() {
        FieldInfo fieldInfo = memoInfo.propertyField("approved");
        assertTrue(fieldInfo.isConvertible());
        AttributeConverter<?, ?> attributeConverter = fieldInfo.converter();
        Date date = new Date(1234567890123L);
        String value = attributeConverter.toGraphProperty(date);
        assertEquals("20090213113130", value);
    }

}
