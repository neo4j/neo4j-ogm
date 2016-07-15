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

package org.neo4j.ogm.metadata;


import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.utils.MetaData;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.domain.hierarchy.domain.trans.TransientSingleClass;

/**
 * @author Mark Angrish
 * @author Luanne Misquitta
 */
public class TransientObjectsTest {

    private MetaData metaData;

    @Before
    public void setUp() {
        metaData = new MetaData("org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.pizza", "org.neo4j.ogm.metadata", "org.neo4j.ogm.domain.canonical", "org.neo4j.ogm.domain.hierarchy.domain", "org.neo4j.ogm.domain.cineasts.annotated");
    }

    @Test
    public void testFieldMarkedWithTransientModifierIsNotInMetaData() {
        ClassInfo classInfo = metaData.classInfo("PersistableClass");
        assertNotNull(classInfo);
        FieldInfo fieldInfo = classInfo.propertyField("transientObject");
        assertNull(fieldInfo);
    }

    @Test
    public void testClassAnnotatedTransientIsExcludedFromMetaData() {
        ClassInfo classInfo = metaData.classInfo("TransientObjectsTest$TransientClass");
        assertNull(classInfo);
    }


    @Test
    public void testMethodAnnotatedTransientIsExcludedFromMetaData() {
        ClassInfo classInfo = metaData.classInfo("PersistableClass");
        MethodInfo methodInfo = classInfo.propertyGetter("transientObject");
        assertNull(methodInfo);
    }

    @Test
    public void testFieldAnnotatedTransientIsExcludedFromMetaData() {
        ClassInfo classInfo = metaData.classInfo("PersistableClass");
        FieldInfo fieldInfo = classInfo.propertyField("chickenCounting");
        assertNull(fieldInfo);
    }

    @Test
    public void testMethodWithTransientReturnTypeIsExcludedFromRelationshipMethods() {
        ClassInfo classInfo = metaData.classInfo("PersistableClass");
        MethodInfo methodInfo = classInfo.relationshipGetter("TRANSIENT_SINGLE_CLASS");
        assertNull(methodInfo);
        methodInfo = classInfo.relationshipSetter("TRANSIENT_SINGLE_CLASS");
        assertNull(methodInfo);
        for (MethodInfo method : classInfo.relationshipGetters()) {
            if (method.getName().equals("getTransientSingleClass")) {
                fail("getTransientSingleClass should not be returned in relationshipGetters");
            }
        }
        for (MethodInfo method : classInfo.relationshipSetters()) {
            if (method.getName().equals("setTransientSingleClass")) {
                fail("getTransientSingleClass should not be returned in relationshipSetters");
            }
        }
    }

    @Test
    public void testMethodWithTransientReturnTypeIsExcludedFromRelationshipFields() {
        ClassInfo classInfo = metaData.classInfo("PersistableClass");
        FieldInfo fieldInfo = classInfo.relationshipField("TRANSIENT_SINGLE_CLASS");
        assertNull(fieldInfo);
        for (FieldInfo field : classInfo.relationshipFields()) {
            if (field.getName().equals("transientSingleClassField")) {
                fail("transientSingleClassField should not be returned in relationshipFields");
            }
        }
    }

    @NodeEntity(label = "PersistableClass")
    public class PersistableClass {

        private Long id;
        private transient String transientObject;

        @Transient
        private Integer chickenCounting;

        public TransientSingleClass transientSingleClassField;

        @Transient
        public String getTransientObject() {
            return transientObject;
        }

        public void setTransientObject(String value) {
            transientObject = value;
        }

        public void setTransientSingleClass(TransientSingleClass transientSingleClass) {

        }

        public TransientSingleClass getTransientSingleClass() {
            return null;
        }


    }

    @Transient
    public class TransientClass {
        private Long id;
    }
}
