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

import org.junit.Test;

/**
 * @author vince
 */
public class AnnotationsTest {

    @Test
    public void shouldLoadMetaDataWithComplexAnnotations() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.annotations");

        assertThat(metaData.classInfo("SimpleNode").name()).isEqualTo("org.neo4j.ogm.domain.annotations.SimpleNode");
        assertThat(metaData.classInfo("OtherNode").name()).isEqualTo("org.neo4j.ogm.domain.annotations.OtherNode");
    }

    @Test
    public void shouldReadIndexAnnotationElement() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.annotations");
        ClassInfo classInfo = metaData.classInfo("IndexedEntity");

        FieldInfo fieldInfo = classInfo.propertyField("ref");
        AnnotationInfo annotationInfo = fieldInfo.getAnnotations()
            .get("org.neo4j.ogm.domain.annotations.IndexedEntity$Indexed");

        // string-y types
        assertThat(annotationInfo.get("b", "")).isEqualTo("97");
        assertThat(annotationInfo.get("c", "")).isEqualTo("1");
        assertThat(annotationInfo.get("t", "")).isEqualTo("t");
        // numeric types
        assertThat(annotationInfo.get("d", "0.0d")).isEqualTo("0.01");
        assertThat(annotationInfo.get("f", "0.0f")).isEqualTo("0.02");
        assertThat(annotationInfo.get("s", "0")).isEqualTo("3");
        assertThat(annotationInfo.get("i", "0")).isEqualTo("5");
        assertThat(annotationInfo.get("j", "0")).isEqualTo("6");
        // boolean
        assertThat(annotationInfo.get("z", "false")).isEqualTo("true");
    }
}
