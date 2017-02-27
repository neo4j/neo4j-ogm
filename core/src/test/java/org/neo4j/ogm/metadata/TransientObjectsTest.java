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
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.domain.hierarchy.domain.trans.TransientSingleClass;

/**
 * @author Mark Angrish
 * @author Luanne Misquitta
 */
public class TransientObjectsTest {

    private MetadataMap metaData;

    @Before
    public void setUp() {
        metaData = new MetadataMap("org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.pizza", "org.neo4j.ogm.metadata", "org.neo4j.ogm.domain.canonical", "org.neo4j.ogm.domain.hierarchy.domain", "org.neo4j.ogm.domain.cineasts.annotated");
    }

    @Test
    public void testFieldMarkedWithTransientModifierIsNotInMetaData() {
        ClassMetadata classInfo = metaData.classInfo("PersistableClass");
        assertNotNull(classInfo);
        FieldMetadata fieldInfo = classInfo.propertyField("transientObject");
        assertNull(fieldInfo);
    }

    @Test
    public void testClassAnnotatedTransientIsExcludedFromMetaData() {
        ClassMetadata classInfo = metaData.classInfo("TransientObjectsTest$TransientClass");
        assertNull(classInfo);
    }

    @Test
    public void testFieldAnnotatedTransientIsExcludedFromMetaData() {
        ClassMetadata classInfo = metaData.classInfo("PersistableClass");
        FieldMetadata fieldInfo = classInfo.propertyField("chickenCounting");
        assertNull(fieldInfo);
    }


    @Test
    public void testMethodWithTransientReturnTypeIsExcludedFromRelationshipFields() {
        ClassMetadata classInfo = metaData.classInfo("PersistableClass");
        FieldMetadata fieldInfo = classInfo.relationshipField("TRANSIENT_SINGLE_CLASS");
        assertNull(fieldInfo);
        for (FieldMetadata field : classInfo.relationshipFields()) {
            if (field.getName().equals("transientSingleClassField")) {
                fail("transientSingleClassField should not be returned in relationshipFields");
            }
        }
    }

    @NodeEntity(label = "PersistableClass")
    public class PersistableClass {

        private Long id;

        @Transient
        private transient String transientObject;

        @Transient
        private Integer chickenCounting;

        public TransientSingleClass transientSingleClassField;

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
