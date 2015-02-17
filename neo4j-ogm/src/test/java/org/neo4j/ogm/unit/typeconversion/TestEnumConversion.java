/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.unit.typeconversion;

import org.junit.Test;
import org.neo4j.ogm.domain.convertible.enums.*;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;
import org.neo4j.ogm.typeconversion.AttributeConverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestEnumConversion {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.enums");
    private static final ClassInfo algebraInfo = metaData.classInfo("Algebra");
    private static final ClassInfo personInfo = metaData.classInfo("Person");

    @Test
    public void testSaveFieldWithAnnotatedConverter() {
        FieldInfo fieldInfo = algebraInfo.propertyField("numberSystem");
        assertTrue(fieldInfo.hasConverter());

        Algebra algebra = new Algebra();
        algebra.setNumberSystem(NumberSystem.NATURAL);
        assertEquals("N", algebra.getNumberSystem().getDomain());
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

        assertEquals(NumberSystem.INTEGER, algebra.getNumberSystem());
        assertEquals("Z", algebra.getNumberSystem().getDomain());
    }

    @Test
    public void testCustomConverter() {
        MethodInfo methodInfo = algebraInfo.propertyGetter("numberSystem");
        assertTrue(methodInfo.hasConverter());
        assertEquals(NumberSystemDomainConverter.class, methodInfo.converter().getClass());

        String domain = "Z";  // an algebraic domain (i.e. the integers)

        Algebra algebra = new Algebra();
        algebra.setNumberSystem((NumberSystem) methodInfo.converter().toEntityAttribute(domain));

        assertEquals(NumberSystem.INTEGER, algebra.getNumberSystem());
        assertEquals("Z", algebra.getNumberSystem().getDomain());
    }

    @Test
    public void testGenderFieldWithAutoDetectedConverter() {

        Person bob = new Person();
        bob.setGender(Gender.MALE);

        FieldInfo fieldInfo = personInfo.propertyField("gender");

        assertTrue(fieldInfo.hasConverter());
        assertEquals("MALE", fieldInfo.converter().toGraphProperty(bob.getGender()));

    }

    @Test
    public void testGenderSetterWithAutoDetectedConverter() {
        Person bob = new Person();
        MethodInfo methodInfo = personInfo.propertySetter("gender");
        assertTrue(methodInfo.hasConverter());
        bob.setGender((Gender) methodInfo.converter().toEntityAttribute("MALE"));
        assertEquals(Gender.MALE, bob.getGender());

    }

    @Test
    public void testGenderGetterWithAutoDetectedConverter() {
        Person bob = new Person();
        bob.setGender(Gender.MALE);
        MethodInfo methodInfo = personInfo.propertyGetter("gender");
        assertTrue(methodInfo.hasConverter());
        assertEquals("MALE", methodInfo.converter().toGraphProperty(bob.getGender()));
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

}
