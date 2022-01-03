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

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.domain.hierarchy.domain.trans.TransientSingleClass;

/**
 * @author Mark Angrish
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class TransientObjectsTest {

    private MetaData metaData;

    @Before
    public void setUp() {
        metaData = new MetaData("org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.pizza", "org.neo4j.ogm.domain.metadata",
            "org.neo4j.ogm.domain.canonical", "org.neo4j.ogm.domain.hierarchy.domain",
            "org.neo4j.ogm.domain.cineasts.annotated");
    }

    @Test
    public void testFieldMarkedWithTransientModifierIsNotInMetaData() {
        ClassInfo classInfo = metaData.classInfo("PersistableClass");
        assertThat(classInfo).isNotNull();
        FieldInfo fieldInfo = classInfo.propertyField("transientObject");
        assertThat(fieldInfo).isNull();
    }

    @Test
    public void testClassAnnotatedTransientIsExcludedFromMetaData() {
        ClassInfo classInfo = metaData.classInfo("TransientObjectsTest$TransientClass");
        assertThat(classInfo).isNull();
    }

    @Test
    public void testFieldAnnotatedTransientIsExcludedFromMetaData() {
        ClassInfo classInfo = metaData.classInfo("PersistableClass");
        FieldInfo fieldInfo = classInfo.propertyField("chickenCounting");
        assertThat(fieldInfo).isNull();
    }

    @Test
    public void testMethodWithTransientReturnTypeIsExcludedFromRelationshipFields() {
        ClassInfo classInfo = metaData.classInfo("PersistableClass");
        FieldInfo fieldInfo = classInfo.relationshipField("TRANSIENT_SINGLE_CLASS");
        assertThat(fieldInfo).isNull();
        for (FieldInfo field : classInfo.relationshipFields()) {
            if (field.getName().equals("transientSingleClassField")) {
                fail("transientSingleClassField should not be returned in relationshipFields");
            }
        }
    }

}
