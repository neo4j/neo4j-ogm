/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.typeconversion;

import org.junit.Test;
import org.neo4j.ogm.metadata.MetadataMap;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.neo4j.ogm.metadata.ClassMetadata;
import org.neo4j.ogm.metadata.FieldMetadata;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class DateConversionTest {

    private static final MetadataMap metaData = new MetadataMap("org.neo4j.ogm.domain.convertible.date");
    private static final ClassMetadata memoInfo = metaData.classInfo("Memo");
    SimpleDateFormat simpleDateISO8601format = new SimpleDateFormat(DateString.ISO_8601);

    @Test
    public void assertFieldDateConversionToISO8601FormatByDefault() {
        FieldMetadata fieldInfo = memoInfo.propertyField("recorded");
        assertTrue(fieldInfo.hasPropertyConverter());
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class));
        assertEquals("1970-01-01T00:00:00.000Z", attributeConverter.toGraphProperty(new Date(0)));
    }

    @Test
    public void assertFieldDateConversionWithUserDefinedFormat() {
        FieldMetadata fieldInfo = memoInfo.propertyField("actioned");
        assertTrue(fieldInfo.hasPropertyConverter());
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class));
        assertEquals("1970-01-01", attributeConverter.toGraphProperty(new Date(0)));
    }

    @Test
    public void assertFieldDateLongConversion() {
        FieldMetadata fieldInfo = memoInfo.propertyField("closed");
        assertTrue(fieldInfo.hasPropertyConverter());
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateLongConverter.class));
        Date date = new Date(0);
        Long value = (Long) attributeConverter.toGraphProperty(date);
        assertEquals(new Long(0), value);
    }

    @Test
    public void assertFieldCustomTypeConversion() {
        FieldMetadata fieldInfo = memoInfo.propertyField("approved");
        assertTrue(fieldInfo.hasPropertyConverter());
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertEquals("20090213113130", attributeConverter.toGraphProperty(new Date(1234567890123L)));
    }

    @Test
    public void assertConvertingNullGraphPropertyWorksCorrectly() {
        FieldMetadata methodInfo = memoInfo.propertyField("approved");
        assertTrue(methodInfo.hasPropertyConverter());
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertEquals(null, attributeConverter.toEntityAttribute(null));
    }

    @Test
    public void assertConvertingNullAttributeWorksCorrectly() {
        FieldMetadata methodInfo = memoInfo.propertyField("approved");
        assertTrue(methodInfo.hasPropertyConverter());
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertEquals(null, attributeConverter.toGraphProperty(null));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertArrayFieldDateConversionToISO8601FormatByDefault() {//here
        simpleDateISO8601format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date[] dates = new Date[]{new Date(0), new Date(20000)};
        FieldMetadata fieldInfo = memoInfo.propertyField("escalations");
        assertTrue(fieldInfo.hasPropertyConverter());
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateArrayStringConverter.class));
        String[] converted = (String[]) attributeConverter.toGraphProperty(dates);
        assertTrue(converted[0].equals("1970-01-01T00:00:00.000Z") || converted[1].equals("1970-01-01T00:00:00.000Z"));
        assertTrue(converted[0].equals(simpleDateISO8601format.format(new Date(20000))) || converted[1].equals(simpleDateISO8601format.format(new Date(20000))));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullArrayGraphPropertyWorksCorrectly() {
        FieldMetadata methodInfo = memoInfo.propertyField("escalations");
        assertTrue(methodInfo.hasPropertyConverter());
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertEquals(null, attributeConverter.toEntityAttribute(null));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullArrayAttributeWorksCorrectly() {
        FieldMetadata methodInfo = memoInfo.propertyField("escalations");
        assertTrue(methodInfo.hasPropertyConverter());
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertEquals(null, attributeConverter.toGraphProperty(null));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertCollectionFieldDateConversionToISO8601FormatByDefault() {//here
        simpleDateISO8601format.setTimeZone(TimeZone.getTimeZone("UTC"));
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(0));
        dates.add(new Date(20000));
        FieldMetadata fieldInfo = memoInfo.propertyField("implementations");
        assertTrue(fieldInfo.hasPropertyConverter());
        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateCollectionStringConverter.class));
        String[] converted = (String[]) attributeConverter.toGraphProperty(dates);
        assertTrue(converted[0].equals("1970-01-01T00:00:00.000Z") || converted[1].equals("1970-01-01T00:00:00.000Z"));
        assertTrue(converted[0].equals(simpleDateISO8601format.format(new Date(20000))) || converted[1].equals(simpleDateISO8601format.format(new Date(20000))));
    }


    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullCollectionGraphPropertyWorksCorrectly() {
        FieldMetadata methodInfo = memoInfo.propertyField("implementations");
        assertTrue(methodInfo.hasPropertyConverter());
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertEquals(null, attributeConverter.toEntityAttribute(null));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullCollectionAttributeWorksCorrectly() {
        FieldMetadata methodInfo = memoInfo.propertyField("implementations");
        assertTrue(methodInfo.hasPropertyConverter());
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertEquals(null, attributeConverter.toGraphProperty(null));
    }
}
