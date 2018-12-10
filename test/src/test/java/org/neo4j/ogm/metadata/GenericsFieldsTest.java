/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.metadata;

import static org.assertj.core.api.Assertions.*;

import java.io.Serializable;
import java.util.Map;

import org.junit.Test;
import org.neo4j.ogm.domain.gh492.BaseUser;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class GenericsFieldsTest extends TestMetaDataTypeResolution {

    @Test
    public void testUnboundedGeneric() {
        checkField("genericObject", "java.lang.Object", Object.class);
    }

    @Test
    public void testGenericComparable() { // from java.lang
        checkField("genericComparable", "java.lang.Comparable", Comparable.class);
    }

    @Test
    public void testGenericSerializable() { // from java.io
        checkField("genericSerializable", "java.io.Serializable", Serializable.class);
    }

    @Test
    public void testGenericSelfReference() {
        checkField("next", "org.neo4j.ogm.metadata.POJO", POJO.class);
    }

    @Test // List<S>
    public void testCollectionWithUnboundGenericParameter() {
        checkField("elements", "java.lang.Object", Object.class);
    }

    @Test // List<POJO<S, T, U>> neighbours;
    public void testCollectionWithConcreteParameterizedType() {
        checkField("neighbours", "org.neo4j.ogm.metadata.POJO", POJO.class);
    }

    @Test // List<? extends Integer> superIntegers
    public void testCollectionWithExtendedConcreteParameterizedType() {
        checkField("superIntegers", "java.lang.Object", Object.class);
    }

    @Test    // List<? super Integer> subIntegers;
    public void testCollectionWithReducedConcreteParameterizedType() {
        checkField("subIntegers", "java.lang.Object", Object.class);
    }

    @Test    // List<? extends S> superS;
    public void testCollectionOfWildcardExtendingGenericType() {
        checkField("superS", "java.lang.Object", Object.class);
    }

    @Test    // List<? super S> subS;
    public void testCollectionOfWildcardReducingGenericType() {
        checkField("subS", "java.lang.Object", Object.class);
    }

    @Test    // List<?>;
    public void testListGenericWildcard() {
        checkField("listOfAnything", "java.lang.Object", Object.class);
    }

    @Test    // Vector<?>;
    public void testVectorGenericWildcard() {
        checkField("vectorOfAnything", "java.lang.Object", Object.class);
    }

    @Test    // Set<?>;
    public void testSetGenericWildcard() {
        checkField("setOfAnything", "java.lang.Object", Object.class);
    }

    @Test    // Iterable<Map<Class<S>, POJO<S, T, U>>> iterable;
    public void testIterableOfMapOfParameterizedClasses() {
        checkField("iterable", "java.util.Map", Map.class);
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
}
