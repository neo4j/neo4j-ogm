/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.typeconversion;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.neo4j.ogm.domain.convertible.enums.Algebra;
import org.neo4j.ogm.domain.convertible.enums.Education;
import org.neo4j.ogm.domain.convertible.enums.Gender;
import org.neo4j.ogm.domain.convertible.enums.NumberSystem;
import org.neo4j.ogm.domain.convertible.enums.Person;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Gerrit Meier
 */
public class EnumConversionTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.convertible.enums");

    private static final ClassInfo algebraInfo = metaData.classInfo("Algebra");
    private static final ClassInfo personInfo = metaData.classInfo("Person");
    private static final ClassInfo tagEntityInfo = metaData.classInfo("TagEntity");

    @Test
    public void testSaveFieldWithAnnotatedConverter() {
        FieldInfo fieldInfo = algebraInfo.propertyField("numberSystem");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();

        Algebra algebra = new Algebra();
        algebra.setNumberSystem(NumberSystem.NATURAL);
        assertThat(algebra.getNumberSystem().getDomain()).isEqualTo("N");
        String value = (String) fieldInfo.getPropertyConverter().toGraphProperty(algebra.getNumberSystem());
        // the converted enum value that will be stored as a neo4j node / rel property
        assertThat(value).isEqualTo("NATURAL");
    }

    @Test
    public void testLoadFieldWithAnnotatedConverter() {
        FieldInfo fieldInfo = algebraInfo.propertyField("numberSystem");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        // a node / rel property value loaded from neo4j, to be stored in on an enum
        String value = "INTEGER";
        Algebra algebra = new Algebra();
        algebra.setNumberSystem((NumberSystem) fieldInfo.getPropertyConverter().toEntityAttribute(value));

        assertThat(algebra.getNumberSystem()).isEqualTo(NumberSystem.INTEGER);
        assertThat(algebra.getNumberSystem().getDomain()).isEqualTo("Z");
    }

    @Test
    public void testGenderFieldWithAutoDetectedConverter() {

        Person bob = new Person();
        bob.setGender(Gender.MALE);

        FieldInfo fieldInfo = personInfo.propertyField("gender");

        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        assertThat(fieldInfo.getPropertyConverter().toGraphProperty(bob.getGender())).isEqualTo("MALE");
    }

    @Test
    public void assertConvertingNullGraphPropertyWorksCorrectly() {
        FieldInfo methodInfo = personInfo.propertyField("gender");
        assertThat(methodInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertThat(attributeConverter.toEntityAttribute(null)).isEqualTo(null);
    }

    @Test
    public void assertConvertingNullAttributeWorksCorrectly() {
        FieldInfo methodInfo = personInfo.propertyField("gender");
        assertThat(methodInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertThat(attributeConverter.toGraphProperty(null)).isEqualTo(null);
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void testEducationArrayFieldWithAutoDetectedConverter() {

        Person bob = new Person();
        bob.setGender(Gender.MALE);
        Education[] inProgress = new Education[] { Education.MASTERS, Education.PHD };
        bob.setInProgressEducation(inProgress);

        FieldInfo fieldInfo = personInfo.propertyField("inProgressEducation");

        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        String[] converted = (String[]) fieldInfo.getPropertyConverter().toGraphProperty(bob.getInProgressEducation());
        assertThat("MASTERS".equals(converted[0]) || "MASTERS".equals(converted[1])).isTrue();
        assertThat("PHD".equals(converted[0]) || "PHD".equals(converted[1])).isTrue();
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullArrayGraphPropertyWorksCorrectly() {
        FieldInfo methodInfo = personInfo.propertyField("inProgressEducation");
        assertThat(methodInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertThat(attributeConverter.toEntityAttribute(null)).isEqualTo(null);
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullArrayAttributeWorksCorrectly() {
        FieldInfo methodInfo = personInfo.propertyField("inProgressEducation");
        assertThat(methodInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertThat(attributeConverter.toGraphProperty(null)).isEqualTo(null);
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

        assertThat(fieldInfo.hasPropertyConverter()).isTrue();
        String[] converted = (String[]) fieldInfo.getPropertyConverter().toGraphProperty(bob.getCompletedEducation());
        assertThat("HIGHSCHOOL".equals(converted[0]) || "HIGHSCHOOL".equals(converted[1])).isTrue();
        assertThat("BACHELORS".equals(converted[0]) || "BACHELORS".equals(converted[1])).isTrue();
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullCollectionGraphPropertyWorksCorrectly() {
        FieldInfo methodInfo = personInfo.propertyField("completedEducation");
        assertThat(methodInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertThat(attributeConverter.toEntityAttribute(null)).isEqualTo(null);
    }

    /**
     * @see DATAGRAPH-550
     */
    @Test
    public void assertConvertingNullCollectionAttributeWorksCorrectly() {
        FieldInfo methodInfo = personInfo.propertyField("completedEducation");
        assertThat(methodInfo.hasPropertyConverter()).isTrue();
        AttributeConverter attributeConverter = methodInfo.getPropertyConverter();
        assertThat(attributeConverter.toGraphProperty(null)).isEqualTo(null);
    }

    /**
     * @see DATAGRAPH-720
     */
    @Test
    public void shouldNotRegisterEnumWhenTypeContainsEnumType() {
        FieldInfo fieldInfo = tagEntityInfo.relationshipFieldByName("tags");
        assertThat(fieldInfo.hasPropertyConverter()).isFalse();
    }

    /**
     * @see issue #424
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldFailOnEmptyGraphProperty() {
        FieldInfo fieldInfo = algebraInfo.propertyField("operation");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();

        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertThat(attributeConverter.toEntityAttribute("")).isNull();
    }

    /**
     * @see issue #424
     */
    @Test
    public void shouldWorkOnEmptyGraphPropertyWithLenientConversionEnabled() {
        FieldInfo fieldInfo = algebraInfo.propertyField("operationLenient");
        assertThat(fieldInfo.hasPropertyConverter()).isTrue();

        AttributeConverter attributeConverter = fieldInfo.getPropertyConverter();
        assertThat(attributeConverter.toEntityAttribute("")).isNull();
    }
}
