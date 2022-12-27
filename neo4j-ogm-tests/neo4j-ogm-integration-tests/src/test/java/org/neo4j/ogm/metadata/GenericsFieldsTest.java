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
package org.neo4j.ogm.metadata;

import static org.assertj.core.api.Assertions.*;

import java.io.Serializable;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.neo4j.ogm.domain.gh492.BaseUser;
import org.neo4j.ogm.domain.gh704.Country;
import org.neo4j.ogm.domain.gh704.CountryRevision;
import org.neo4j.ogm.domain.metadata.POJO;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class GenericsFieldsTest extends TestMetaDataTypeResolution {

    @Test
    void testUnboundedGeneric() {
        checkField("genericObject", "java.lang.Object", Object.class, null);
    }

    @Test
    void testGenericComparable() { // from java.lang
        checkField("genericComparable", "java.lang.Comparable", Comparable.class, null);
    }

    @Test
    void testGenericSerializable() { // from java.io
        checkField("genericSerializable", "java.io.Serializable", Serializable.class, null);
    }

    @Test
    void testGenericSelfReference() {
        checkField("next", "org.neo4j.ogm.domain.metadata.POJO", POJO.class, null);
    }

    // List<S>
    @Test
    void testCollectionWithUnboundGenericParameter() {
        checkField("elements", "java.lang.Object", Object.class, "java.util.List");
    }

    // List<POJO<S, T, U>> neighbours;
    @Test
    void testCollectionWithConcreteParameterizedType() {
        checkField("neighbours", "org.neo4j.ogm.domain.metadata.POJO", POJO.class, "java.util.List");
    }

    // List<? extends Integer> superIntegers
    @Test
    void testCollectionWithExtendedConcreteParameterizedType() {
        checkField("superIntegers", "java.lang.Object", Object.class, "java.util.List");
    }

    // List<? super Integer> subIntegers;
    @Test
    void testCollectionWithReducedConcreteParameterizedType() {
        checkField("subIntegers", "java.lang.Object", Object.class, "java.util.List");
    }

    // List<? extends S> superS;
    @Test
    void testCollectionOfWildcardExtendingGenericType() {
        checkField("superS", "java.lang.Object", Object.class, "java.util.List");
    }

    // List<? super S> subS;
    @Test
    void testCollectionOfWildcardReducingGenericType() {
        checkField("subS", "java.lang.Object", Object.class, "java.util.List");
    }

    // List<?>;
    @Test
    void testListGenericWildcard() {
        checkField("listOfAnything", "java.lang.Object", Object.class, "java.util.List");
    }

    // Vector<?>;
    @Test
    void testVectorGenericWildcard() {
        checkField("vectorOfAnything", "java.lang.Object", Object.class, "java.util.Vector");
    }

    // Set<?>;
    @Test
    void testSetGenericWildcard() {
        checkField("setOfAnything", "java.lang.Object", Object.class, "java.util.Set");
    }

    // Iterable<Map<Class<S>, POJO<S, T, U>>> iterable;
    @Test
    void testIterableOfMapOfParameterizedClasses() {
        checkField("iterable", "java.util.Map", Map.class, "java.lang.Iterable");
    }

    // GH-492
    @Test
    void shouldDetectPrimitiveArraysInGenericFields() {
        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh492");

        ClassInfo classInfo = metaData.classInfo(BaseUser.IntUser.class);
        FieldInfo fieldInfo = classInfo.getFieldInfo("genericValue");
        assertThat(fieldInfo.isArray()).isTrue();
        assertThat(fieldInfo.type()).isEqualTo(int[].class);
    }

    // GH-492
    @Test
    void shouldDetectWrapperArraysInGenericFields() {
        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh492");

        ClassInfo classInfo = metaData.classInfo(BaseUser.IntegerUser.class);
        FieldInfo fieldInfo = classInfo.getFieldInfo("genericValue");
        assertThat(fieldInfo.isArray()).isTrue();
        assertThat(fieldInfo.type()).isEqualTo(Integer[].class);
    }

    // GH-656
    @Test
    void parameterizedFieldsInParentClassesShouldWork() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh656");
        ClassInfo classInfo = metaData.classInfo("Group");
        assertThat(classInfo).isNotNull();
        assertThat(classInfo.getFieldInfo("uuid")).isNotNull();
        FieldInfo hasVersionField = classInfo.relationshipField("HAS_VERSION");
        assertThat(hasVersionField).isNotNull();
        assertThat(hasVersionField.getCollectionClassname()).isEqualTo("java.util.Set");
        assertThat(hasVersionField.getTypeDescriptor()).isEqualTo("org.neo4j.ogm.domain.gh656.GroupVersion");
    }

    // GH-706
    @Test
    void parameterizedScalarFieldsInParentClassesShouldWork() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh704");
        for (String[] parameters : new String[][]{
            {"Country", "org.neo4j.ogm.domain.gh704.CountryRevision"},
            {"org.neo4j.ogm.domain.gh704.CountryRevision", "org.neo4j.ogm.domain.gh704.CountryRevision"},
            {"org.neo4j.ogm.domain.gh704.RevisionEntity", "java.lang.Object"}
        }) {
            ClassInfo classInfo = metaData.classInfo(parameters[0]);
            assertThat(classInfo).isNotNull();
            assertThat(classInfo.getFieldInfo("previousRevision")).satisfies(field -> {
                assertThat(field).isNotNull();
                assertThat(field.getTypeDescriptor()).isEqualTo(parameters[1]);
                assertThat(field.relationship()).isEqualTo("PREVIOUS_REVISION");
            });
        }
    }

    // GH-704
    @Test
    void correctClassesNeedToBeUsedDuringFieldLookup() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh704");

        Country old = new Country();
        old.setName("old");

        Country newCountry = new Country();
        newCountry.setName("new");
        newCountry.setPreviousRevision(old);

        CountryRevision newCountryRevision = new CountryRevision();
        newCountry.setName("new");
        newCountry.setPreviousRevision(old);

        for (CountryRevision countryRevision : new CountryRevision[]{newCountry, newCountryRevision}) {
            ClassInfo classInfo = metaData.classInfo(countryRevision.getClass());
            CountryRevision oldRevision = (CountryRevision) classInfo.getFieldInfo("previousRevision").read(newCountry);
            assertThat(oldRevision).isSameAs(old);
        }
    }
}
