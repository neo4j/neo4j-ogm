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

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.annotation.typeconversion.DateString;
import org.neo4j.ogm.domain.convertible.date.Memo;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MethodInfo;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class DateConversionTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.date");
    private static final ClassInfo memoInfo = metaData.classInfo("Memo");
    SimpleDateFormat simpleDateISO8601format = new SimpleDateFormat(DateString.ISO_8601);

    @Test
    public void assertFieldDateConversionToISO8601FormatByDefault() {
        FieldInfo fieldInfo = memoInfo.propertyField("recorded");
        assertTrue(fieldInfo.hasConverter());
        AttributeConverter attributeConverter = fieldInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class));
        assertEquals("1970-01-01T00:00:00.000Z", attributeConverter.toGraphProperty(new Date(0)));
    }

    @Test
    public void assertFieldDateConversionWithUserDefinedFormat() {
        FieldInfo fieldInfo = memoInfo.propertyField("actioned");
        assertTrue(fieldInfo.hasConverter());
        AttributeConverter attributeConverter = fieldInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class));
        assertEquals("1970-01-01", attributeConverter.toGraphProperty(new Date(0)));
    }

    @Test
    public void assertFieldDateLongConversion() {
        FieldInfo fieldInfo = memoInfo.propertyField("closed");
        assertTrue(fieldInfo.hasConverter());
        AttributeConverter attributeConverter = fieldInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateLongConverter.class));
        Date date = new Date(0);
        Long value = (Long) attributeConverter.toGraphProperty(date);
        assertEquals(new Long(0), value);
    }

    @Test
    public void assertFieldCustomTypeConversion() {
        FieldInfo fieldInfo = memoInfo.propertyField("approved");
        assertTrue(fieldInfo.hasConverter());
        AttributeConverter attributeConverter = fieldInfo.converter();
        assertEquals("20090213113130", attributeConverter.toGraphProperty(new Date(1234567890123L)));
    }

    @Test
    public void assertMethodDateConversionToISO8601FormatByDefault() {
        MethodInfo methodInfo = memoInfo.propertyGetter("recorded");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class));
        assertEquals("1970-01-01T00:00:00.000Z", attributeConverter.toGraphProperty(new Date(0)));
    }

    @Test
    public void assertMethodDateConversionWithUserDefinedFormat() {
        MethodInfo methodInfo = memoInfo.propertySetter("actioned");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateStringConverter.class));
        Date date = new Date(0);
        String value = (String) attributeConverter.toGraphProperty(date);
        assertEquals("1970-01-01", value);
    }

    @Test
    public void assertMethodDateLongConversion() {
        MethodInfo methodInfo = memoInfo.propertyGetter("closed");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateLongConverter.class));
        Date date = new Date(0);
        assertEquals(new Long(0), attributeConverter.toGraphProperty(date));
    }

    @Test
    public void assertMethodCustomTypeConversion() {
        MethodInfo methodInfo = memoInfo.propertySetter("approved");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        Date date = new Date(1234567890123L);
        assertEquals("20090213113130", attributeConverter.toGraphProperty(date));
    }

    @Test
    public void assertConvertingNullGraphPropertyWorksCorrectly() {
        MethodInfo methodInfo = memoInfo.propertySetter("approved");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertEquals(null, attributeConverter.toEntityAttribute(null));
    }

    @Test
    public void assertConvertingNullAttributeWorksCorrectly() {
        MethodInfo methodInfo = memoInfo.propertySetter("approved");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertEquals(null, attributeConverter.toGraphProperty(null));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertArrayFieldDateConversionToISO8601FormatByDefault() {//here
        simpleDateISO8601format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date[] dates = new Date[]{new Date(0), new Date(20000)};
        FieldInfo fieldInfo = memoInfo.propertyField("escalations");
        assertTrue(fieldInfo.hasConverter());
        AttributeConverter attributeConverter = fieldInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateArrayStringConverter.class));
        String[] converted = (String[]) attributeConverter.toGraphProperty(dates);
        assertTrue(converted[0].equals("1970-01-01T00:00:00.000Z") || converted[1].equals("1970-01-01T00:00:00.000Z"));
        assertTrue(converted[0].equals(simpleDateISO8601format.format(new Date(20000))) || converted[1].equals(simpleDateISO8601format.format(new Date(20000))));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertMethodGetterDateArrayConversionToISO8601FormatByDefault() {
        simpleDateISO8601format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date[] dates = new Date[]{new Date(0), new Date(20000)};

        MethodInfo methodInfo = memoInfo.propertyGetter("escalations");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateArrayStringConverter.class));
        String[] converted = (String[]) attributeConverter.toGraphProperty(dates);
        assertTrue(converted[0].equals("1970-01-01T00:00:00.000Z") || converted[1].equals("1970-01-01T00:00:00.000Z"));
        assertTrue(converted[0].equals(simpleDateISO8601format.format(new Date(20000))) || converted[1].equals(simpleDateISO8601format.format(new Date(20000))));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertMethodSetterDateArrayConversionToISO8601FormatByDefault() {
        simpleDateISO8601format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date[] dates = new Date[]{new Date(0), new Date(20000)};
        String[] stringDates = new String[]{"1970-01-01T00:00:00.000Z", simpleDateISO8601format.format(new Date(20000))};

        Memo memo = new Memo();
        MethodInfo methodInfo = memoInfo.propertySetter("escalations");
        assertTrue(methodInfo.hasConverter());
        memo.setEscalations((Date[]) methodInfo.converter().toEntityAttribute(stringDates));
        Assert.assertArrayEquals(dates, memo.getEscalations());

    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullArrayGraphPropertyWorksCorrectly() {
        MethodInfo methodInfo = memoInfo.propertyGetter("escalations");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertEquals(null, attributeConverter.toEntityAttribute(null));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullArrayAttributeWorksCorrectly() {
        MethodInfo methodInfo = memoInfo.propertyGetter("escalations");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
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
        FieldInfo fieldInfo = memoInfo.propertyField("implementations");
        assertTrue(fieldInfo.hasConverter());
        AttributeConverter attributeConverter = fieldInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateCollectionStringConverter.class));
        String[] converted = (String[]) attributeConverter.toGraphProperty(dates);
        assertTrue(converted[0].equals("1970-01-01T00:00:00.000Z") || converted[1].equals("1970-01-01T00:00:00.000Z"));
        assertTrue(converted[0].equals(simpleDateISO8601format.format(new Date(20000))) || converted[1].equals(simpleDateISO8601format.format(new Date(20000))));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertMethodGetterDateCollectionConversionToISO8601FormatByDefault() {
        simpleDateISO8601format.setTimeZone(TimeZone.getTimeZone("UTC"));
        List<Date> dates = new ArrayList<>();
        dates.add(new Date(0));
        dates.add(new Date(20000));

        MethodInfo methodInfo = memoInfo.propertyGetter("implementations");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertTrue(attributeConverter.getClass().isAssignableFrom(DateCollectionStringConverter.class));
        String[] converted = (String[]) attributeConverter.toGraphProperty(dates);
        assertTrue(converted[0].equals("1970-01-01T00:00:00.000Z") || converted[1].equals("1970-01-01T00:00:00.000Z"));
        assertTrue(converted[0].equals(simpleDateISO8601format.format(new Date(20000))) || converted[1].equals(simpleDateISO8601format.format(new Date(20000))));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertMethodSetterDateCollectionConversionToISO8601FormatByDefault() {
        simpleDateISO8601format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Set<Date> dates = new HashSet<>();
        dates.add(new Date(0));
        dates.add(new Date(20000));

        String[] stringDates = new String[]{"1970-01-01T00:00:00.000Z", simpleDateISO8601format.format(new Date(20000))};

        Memo memo = new Memo();
        MethodInfo methodInfo = memoInfo.propertySetter("implementations");
        assertTrue(methodInfo.hasConverter());
        memo.setImplementations((Set<Date>) methodInfo.converter().toEntityAttribute(stringDates));
        Assert.assertEquals(dates, memo.getImplementations());

    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullCollectionGraphPropertyWorksCorrectly() {
        MethodInfo methodInfo = memoInfo.propertyGetter("implementations");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertEquals(null, attributeConverter.toEntityAttribute(null));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullCollectionAttributeWorksCorrectly() {
        MethodInfo methodInfo = memoInfo.propertyGetter("implementations");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertEquals(null, attributeConverter.toGraphProperty(null));
    }
}
