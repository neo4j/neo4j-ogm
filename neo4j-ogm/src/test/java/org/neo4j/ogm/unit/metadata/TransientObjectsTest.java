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

package org.neo4j.ogm.unit.metadata;

import org.junit.Test;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.FieldInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

public class TransientObjectsTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.unit.metadata");

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

    @NodeEntity(label="PersistableClass")
    public class PersistableClass {

        private Long id;
        private transient String transientObject;

        @Transient
        private Integer chickenCounting;
        
        @Transient
        public String getTransientObject() {
            return transientObject;
        }

        public void setTransientObject(String value) {
            transientObject = value;
        }
    }

    @Transient
    public class TransientClass {
        private Long id;
    }
}
