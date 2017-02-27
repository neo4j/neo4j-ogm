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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author vince
 */
public class AnnotationsTest {


    @Test
    public void shouldLoadMetaDataWithComplexAnnotations() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.annotations");

        assertEquals("org.neo4j.ogm.domain.annotations.SimpleNode", metaData.classInfo("SimpleNode").name());
        assertEquals("org.neo4j.ogm.domain.annotations.OtherNode", metaData.classInfo("OtherNode").name());

    }

    @Test
    public void shouldReadIndexAnnotationElement() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.annotations");
        ClassInfo classInfo = metaData.classInfo("IndexedEntity");

        FieldInfo fieldInfo = classInfo.propertyField("ref");
        AnnotationInfo annotationInfo = fieldInfo.getAnnotations().get("org.neo4j.ogm.domain.annotations.IndexedEntity$Indexed");

        // string-y types
        assertEquals("97", annotationInfo.get("b", ""));
        assertEquals("1", annotationInfo.get("c", ""));
        assertEquals("t", annotationInfo.get("t", ""));
        // numeric types
        assertEquals("0.01", annotationInfo.get("d", "0.0d"));
        assertEquals("0.02", annotationInfo.get("f", "0.0f"));
        assertEquals("3", annotationInfo.get("s", "0"));
        assertEquals("5", annotationInfo.get("i", "0"));
        assertEquals("6", annotationInfo.get("j", "0"));
        // boolean
        assertEquals("true", annotationInfo.get("z", "false"));
    }


}
