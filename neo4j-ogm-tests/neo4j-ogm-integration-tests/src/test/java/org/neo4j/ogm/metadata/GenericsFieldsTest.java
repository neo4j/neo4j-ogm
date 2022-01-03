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

import org.junit.Test;
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
    public void testUnboundedGeneric() {
        checkField("genericObject", "java.lang.Object", Object.class, null);
    }

    @Test
    public void testGenericComparable() { // from java.lang
        checkField("genericComparable", "java.lang.Comparable", Comparable.class, null);
    }

    @Test
    public void testGenericSerializable() { // from java.io
        checkField("genericSerializable", "java.io.Serializable", Serializable.class, null);
    }

    @Test
    public void testGenericSelfReference() {
        checkField("next", "org.neo4j.ogm.domain.metadata.POJO", POJO.class, null);
    }

    @Test // List<S>
    public void testCollectionWithUnboundGenericParameter() {
        checkField("elements", "java.lang.Object", Object.class, "java.util.List");
    }

    @Test // List<POJO<S, T, U>> neighbours;
    public void testCollectionWithConcreteParameterizedType() {
        checkField("neighbours", "org.neo4j.ogm.domain.metadata.POJO", POJO.class, "java.util.List");
    }

    @Test // List<? extends Integer> superIntegers
    public void testCollectionWithExtendedConcreteParameterizedType() {
        checkField("superIntegers", "java.lang.Object", Object.class, "java.util.List");
    }

    @Test    // List<? super Integer> subIntegers;
    public void testCollectionWithReducedConcreteParameterizedType() {
        checkField("subIntegers", "java.lang.Object", Object.class, "java.util.List");
    }

    @Test    // List<? extends S> superS;
    public void testCollectionOfWildcardExtendingGenericType() {
        checkField("superS", "java.lang.Object", Object.class, "java.util.List");
    }

    @Test    // List<? super S> subS;
    public void testCollectionOfWildcardReducingGenericType() {
        checkField("subS", "java.lang.Object", Object.class, "java.util.List");
    }

    @Test    // List<?>;
    public void testListGenericWildcard() {
        checkField("listOfAnything", "java.lang.Object", Object.class, "java.util.List");
    }

    @Test    // Vector<?>;
    public void testVectorGenericWildcard() {
        checkField("vectorOfAnything", "java.lang.Object", Object.class, "java.util.Vector");
    }

    @Test    // Set<?>;
    public void testSetGenericWildcard() {
        checkField("setOfAnything", "java.lang.Object", Object.class, "java.util.Set");
    }

    @Test    // Iterable<Map<Class<S>, POJO<S, T, U>>> iterable;
    public void testIterableOfMapOfParameterizedClasses() {
        checkField("iterable", "java.util.Map", Map.class, "java.lang.Iterable");
    }

    @Test // GH-492
    public void shouldDetectPrimitiveArraysInGenericFields() {
        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh492");

        ClassInfo classInfo = metaData.classInfo(BaseUser.IntUser.class);
        FieldInfo fieldInfo = classInfo.getFieldInfo("genericValue");
        assertThat(fieldInfo.isArray()).isTrue();
        assertThat(fieldInfo.type()).isEqualTo(int[].class);
    }

    @Test // GH-492
    public void shouldDetectWrapperArraysInGenericFields() {
        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh492");

        ClassInfo classInfo = metaData.classInfo(BaseUser.IntegerUser.class);
        FieldInfo fieldInfo = classInfo.getFieldInfo("genericValue");
        assertThat(fieldInfo.isArray()).isTrue();
        assertThat(fieldInfo.type()).isEqualTo(Integer[].class);
    }

    @Test // GH-656
    public void parameterizedFieldsInParentClassesShouldWork() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh656");
        ClassInfo classInfo = metaData.classInfo("Group");
        assertThat(classInfo).isNotNull();
        assertThat(classInfo.getFieldInfo("uuid")).isNotNull();
        FieldInfo hasVersionField = classInfo.relationshipField("HAS_VERSION");
        assertThat(hasVersionField).isNotNull();
        assertThat(hasVersionField.getCollectionClassname()).isEqualTo("java.util.Set");
        assertThat(hasVersionField.getTypeDescriptor()).isEqualTo("org.neo4j.ogm.domain.gh656.GroupVersion");
    }

    @Test // GH-706
    public void parameterizedScalarFieldsInParentClassesShouldWork() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh704");
        for (String[] parameters : new String[][] {
            { "Country", "org.neo4j.ogm.domain.gh704.CountryRevision" },
            { "org.neo4j.ogm.domain.gh704.CountryRevision", "org.neo4j.ogm.domain.gh704.CountryRevision" },
            { "org.neo4j.ogm.domain.gh704.RevisionEntity", "java.lang.Object" }
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

    @Test // GH-704
    public void correctClassesNeedToBeUsedDuringFieldLookup() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh704");

        Country old = new Country();
        old.setName("old");

        Country newCountry = new Country();
        newCountry.setName("new");
        newCountry.setPreviousRevision(old);

        CountryRevision newCountryRevision = new CountryRevision();
        newCountry.setName("new");
        newCountry.setPreviousRevision(old);

        for (CountryRevision countryRevision : new CountryRevision[] { newCountry, newCountryRevision }) {
            ClassInfo classInfo = metaData.classInfo(countryRevision.getClass());
            CountryRevision oldRevision = (CountryRevision) classInfo.getFieldInfo("previousRevision").read(newCountry);
            assertThat(oldRevision).isSameAs(old);
        }
    }
}
