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
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.domain.convertible.enums.*;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MethodInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class EnumConversionTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.enums");

    private static final ClassInfo algebraInfo = metaData.classInfo("Algebra");
    private static final ClassInfo personInfo = metaData.classInfo("Person");
    private static final ClassInfo tagEntityInfo = metaData.classInfo("TagEntity");

    @Test
    public void testSaveFieldWithAnnotatedConverter() {
        FieldInfo fieldInfo = algebraInfo.propertyField("numberSystem");
        assertTrue(fieldInfo.hasConverter());

        Algebra algebra = new Algebra();
        algebra.setNumberSystem(NumberSystem.NATURAL);
        Assert.assertEquals("N", algebra.getNumberSystem().getDomain());
        String value = (String) fieldInfo.converter().toGraphProperty(algebra.getNumberSystem());
        // the converted enum value that will be stored as a neo4j node / rel property
        assertEquals("NATURAL", value);
    }

    @Test
    public void testLoadFieldWithAnnotatedConverter() {
        FieldInfo fieldInfo = algebraInfo.propertyField("numberSystem");
        assertTrue(fieldInfo.hasConverter());
        // a node / rel property value loaded from neo4j, to be stored in on an enum
        String value = "INTEGER";
        Algebra algebra = new Algebra();
        algebra.setNumberSystem((NumberSystem) fieldInfo.converter().toEntityAttribute(value));

        Assert.assertEquals(NumberSystem.INTEGER, algebra.getNumberSystem());
        Assert.assertEquals("Z", algebra.getNumberSystem().getDomain());
    }

    @Test
    public void testCustomConverter() {
        MethodInfo methodInfo = algebraInfo.propertyGetter("numberSystem");
        assertTrue(methodInfo.hasConverter());
        Assert.assertEquals(NumberSystemDomainConverter.class, methodInfo.converter().getClass());

        String domain = "Z";  // an algebraic domain (i.e. the integers)

        Algebra algebra = new Algebra();
        algebra.setNumberSystem((NumberSystem) methodInfo.converter().toEntityAttribute(domain));

        Assert.assertEquals(NumberSystem.INTEGER, algebra.getNumberSystem());
        Assert.assertEquals("Z", algebra.getNumberSystem().getDomain());
    }

    @Test
    public void testGenderFieldWithAutoDetectedConverter() {

        Person bob = new Person();
        bob.setGender(Gender.MALE);

        FieldInfo fieldInfo = personInfo.propertyField("gender");

        assertTrue(fieldInfo.hasConverter());
        Assert.assertEquals("MALE", fieldInfo.converter().toGraphProperty(bob.getGender()));

    }

    @Test
    public void testGenderSetterWithAutoDetectedConverter() {
        Person bob = new Person();
        MethodInfo methodInfo = personInfo.propertySetter("gender");
        assertTrue(methodInfo.hasConverter());
        bob.setGender((Gender) methodInfo.converter().toEntityAttribute("MALE"));
        Assert.assertEquals(Gender.MALE, bob.getGender());

    }

    @Test
    public void testGenderGetterWithAutoDetectedConverter() {
        Person bob = new Person();
        bob.setGender(Gender.MALE);
        MethodInfo methodInfo = personInfo.propertyGetter("gender");
        assertTrue(methodInfo.hasConverter());
        Assert.assertEquals("MALE", methodInfo.converter().toGraphProperty(bob.getGender()));
    }

    @Test
    public void assertConvertingNullGraphPropertyWorksCorrectly() {
        MethodInfo methodInfo = personInfo.propertyGetter("gender");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertEquals(null, attributeConverter.toEntityAttribute(null));
    }

    @Test
    public void assertConvertingNullAttributeWorksCorrectly() {
        MethodInfo methodInfo = personInfo.propertyGetter("gender");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertEquals(null, attributeConverter.toGraphProperty(null));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void testEducationArrayFieldWithAutoDetectedConverter() {

        Person bob = new Person();
        bob.setGender(Gender.MALE);
        Education[] inProgress = new Education[]{Education.MASTERS, Education.PHD};
        bob.setInProgressEducation(inProgress);

        FieldInfo fieldInfo = personInfo.propertyField("inProgressEducation");

        assertTrue(fieldInfo.hasConverter());
        String[] converted = (String[]) fieldInfo.converter().toGraphProperty(bob.getInProgressEducation());
        assertTrue("MASTERS".equals(converted[0]) || "MASTERS".equals(converted[1]));
        assertTrue("PHD".equals(converted[0]) || "PHD".equals(converted[1]));

    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void testEducationArraySetterWithAutoDetectedConverter() {
        Person bob = new Person();
        Education[] inProgress = new Education[]{Education.MASTERS, Education.PHD};
        MethodInfo methodInfo = personInfo.propertySetter("inProgressEducation");
        assertTrue(methodInfo.hasConverter());
        bob.setInProgressEducation((Education[]) methodInfo.converter().toEntityAttribute(new String[]{"MASTERS", "PHD"}));
        Assert.assertArrayEquals(inProgress, bob.getInProgressEducation());

    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void testEducationArrayGetterWithAutoDetectedConverter() {
        Person bob = new Person();
        Education[] inProgress = new Education[]{Education.MASTERS, Education.PHD};
        bob.setInProgressEducation(inProgress);
        MethodInfo methodInfo = personInfo.propertyGetter("inProgressEducation");
        assertTrue(methodInfo.hasConverter());
        String[] converted = (String[]) methodInfo.converter().toGraphProperty(bob.getInProgressEducation());
        assertTrue("MASTERS".equals(converted[0]) || "MASTERS".equals(converted[1]));
        assertTrue("PHD".equals(converted[0]) || "PHD".equals(converted[1]));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullArrayGraphPropertyWorksCorrectly() {
        MethodInfo methodInfo = personInfo.propertyGetter("inProgressEducation");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertEquals(null, attributeConverter.toEntityAttribute(null));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullArrayAttributeWorksCorrectly() {
        MethodInfo methodInfo = personInfo.propertyGetter("inProgressEducation");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertEquals(null, attributeConverter.toGraphProperty(null));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void testEducationCollectionFieldWithAutoDetectedConverter() {
        List<Education> completedEducation = new ArrayList<>();
        completedEducation.add(Education.HIGHSCHOOL);
        completedEducation.add(Education.BACHELORS);

        Person bob = new Person();
        bob.setCompletedEducation(completedEducation);

        FieldInfo fieldInfo = personInfo.propertyField("completedEducation");

        assertTrue(fieldInfo.hasConverter());
        String[] converted = (String[]) fieldInfo.converter().toGraphProperty(bob.getCompletedEducation());
        assertTrue("HIGHSCHOOL".equals(converted[0]) || "HIGHSCHOOL".equals(converted[1]));
        assertTrue("BACHELORS".equals(converted[0]) || "BACHELORS".equals(converted[1]));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void testEducationCollectionSetterWithAutoDetectedConverter() {
        Person bob = new Person();
        MethodInfo methodInfo = personInfo.propertySetter("completedEducation");
        assertTrue(methodInfo.hasConverter());
        bob.setCompletedEducation((List) methodInfo.converter().toEntityAttribute(new String[]{"HIGHSCHOOL", "BACHELORS"}));
        assertTrue(bob.getCompletedEducation().contains(Education.HIGHSCHOOL));
        assertTrue(bob.getCompletedEducation().contains(Education.BACHELORS));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void testGenderCollectionGetterWithAutoDetectedConverter() {
        Person bob = new Person();
        List<Education> completed = Arrays.asList(Education.HIGHSCHOOL, Education.BACHELORS);
        bob.setCompletedEducation(completed);
        MethodInfo methodInfo = personInfo.propertySetter("completedEducation");
        assertTrue(methodInfo.hasConverter());
        String[] converted = (String[]) methodInfo.converter().toGraphProperty(bob.getCompletedEducation());
        assertTrue("HIGHSCHOOL".equals(converted[0]) || "HIGHSCHOOL".equals(converted[1]));
        assertTrue("BACHELORS".equals(converted[0]) || "BACHELORS".equals(converted[1]));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullCollectionGraphPropertyWorksCorrectly() {
        MethodInfo methodInfo = personInfo.propertyGetter("completedEducation");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertEquals(null, attributeConverter.toEntityAttribute(null));
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullCollectionAttributeWorksCorrectly() {
        MethodInfo methodInfo = personInfo.propertyGetter("completedEducation");
        assertTrue(methodInfo.hasConverter());
        AttributeConverter attributeConverter = methodInfo.converter();
        assertEquals(null, attributeConverter.toGraphProperty(null));
    }

    /**
     * @see DATAGRAPH-720
     */
    @Test
    public void shouldNotRegisterEnumWhenTypeContainsEnumType() {
        FieldInfo fieldInfo = tagEntityInfo.relationshipFieldByName("tags");
        assertFalse(fieldInfo.hasConverter());
    }
}
