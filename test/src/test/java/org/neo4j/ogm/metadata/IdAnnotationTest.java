/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.annotations.ids.ValidAnnotations;
import org.neo4j.ogm.id.UuidStrategy;

public class IdAnnotationTest {

    private MetaData metaData;

    @Before
    public void setUp() throws Exception {
        metaData = new MetaData("org.neo4j.ogm.domain.annotations.ids");
    }

    @Test
    //    @Ignore("Classes without graph id field not implemented yet")
    public void shouldSupportClassWithoutId() throws Exception {

        ValidAnnotations.WithoutId entity = new ValidAnnotations.WithoutId();

        ClassInfo classInfo = metaData.classInfo(entity);
        assertThat(classInfo.identityFieldOrNull()).isNull();
        assertThat(classInfo.primaryIndexField().getName()).isEqualTo("identifier");
    }

    @Test
    public void shouldFindInternalIdentifier() throws Exception {
        ValidAnnotations.InternalIdWithAnnotation entity = new ValidAnnotations.InternalIdWithAnnotation();
        ClassInfo classInfo = metaData.classInfo(entity);
        // primary index field should be null, @Id is internal
        assertThat(classInfo.primaryIndexField()).isNull();
    }

    @Test
    public void shouldFindBasicId() throws Exception {

        ValidAnnotations.Basic entity = new ValidAnnotations.Basic();

        ClassInfo classInfo = metaData.classInfo(entity);
        assertThat(classInfo.primaryIndexField().getName()).isNotNull().isEqualTo("identifier");
    }

    @Test
    public void shouldFindBasicChild() throws Exception {

        ValidAnnotations.BasicChild entity = new ValidAnnotations.BasicChild();

        ClassInfo classInfo = metaData.classInfo(entity);
        assertThat(classInfo.primaryIndexField().getName()).isNotNull().isEqualTo("identifier");
    }

    @Test
    public void shouldFindIdAndGenerationType() throws Exception {

        ValidAnnotations.IdAndGenerationType entity = new ValidAnnotations.IdAndGenerationType();

        ClassInfo classInfo = metaData.classInfo(entity);
        assertThat(classInfo.primaryIndexField()).isNotNull();
        assertThat(classInfo.idStrategy()).isNotNull()
            .isInstanceOf(UuidStrategy.class);
    }
}
