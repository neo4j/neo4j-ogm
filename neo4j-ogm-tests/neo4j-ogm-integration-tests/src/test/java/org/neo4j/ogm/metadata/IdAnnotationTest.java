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
import org.neo4j.ogm.domain.annotations.ids.ValidAnnotations;
import org.neo4j.ogm.id.UuidStrategy;

/**
 * @author Nicolas Mervaillie
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class IdAnnotationTest {

    private MetaData metaData;

    @Before
    public void setUp() {
        metaData = new MetaData("org.neo4j.ogm.domain.annotations.ids");
    }

    @Test
    public void shouldSupportClassWithoutId() {

        ValidAnnotations.WithoutId entity = new ValidAnnotations.WithoutId();

        ClassInfo classInfo = metaData.classInfo(entity);
        assertThat(classInfo.identityFieldOrNull()).isNull();
        assertThat(classInfo.primaryIndexField().getName()).isEqualTo("identifier");
    }

    @Test
    public void shouldFindInternalIdentifier() {
        ValidAnnotations.InternalIdWithAnnotation entity = new ValidAnnotations.InternalIdWithAnnotation();
        ClassInfo classInfo = metaData.classInfo(entity);
        // primary index field should be null, @Id is internal
        assertThat(classInfo.primaryIndexField()).isNull();
    }

    @Test
    public void shouldFindBasicId() {

        ValidAnnotations.Basic entity = new ValidAnnotations.Basic();

        ClassInfo classInfo = metaData.classInfo(entity);
        assertThat(classInfo.primaryIndexField().getName()).isNotNull().isEqualTo("identifier");
    }

    @Test
    public void shouldFindBasicChild() {

        ValidAnnotations.BasicChild entity = new ValidAnnotations.BasicChild();

        ClassInfo classInfo = metaData.classInfo(entity);
        assertThat(classInfo.primaryIndexField().getName()).isNotNull().isEqualTo("identifier");
    }

    @Test
    public void shouldFindIdAndGenerationType() {

        ValidAnnotations.IdAndGenerationType entity = new ValidAnnotations.IdAndGenerationType();

        ClassInfo classInfo = metaData.classInfo(entity);
        assertThat(classInfo.primaryIndexField()).isNotNull();
        assertThat(classInfo.idStrategy()).isNotNull()
            .isInstanceOf(UuidStrategy.class);
    }
}
