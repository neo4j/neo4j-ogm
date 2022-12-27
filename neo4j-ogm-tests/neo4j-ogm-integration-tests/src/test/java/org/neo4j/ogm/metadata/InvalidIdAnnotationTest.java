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

import static org.junit.jupiter.api.Assertions.assertThrows;


import org.junit.jupiter.api.Test;
import org.neo4j.ogm.domain.annotations.ids.ValidAnnotations;
import org.neo4j.ogm.domain.invalid.ids.InvalidAnnotations;
import org.neo4j.ogm.exception.core.MetadataException;

public class InvalidIdAnnotationTest {

    private MetaData metaData;

    @Test
    void shouldRejectTwoIdsOnSameClass() {
        assertThrows(MetadataException.class, () -> {
            createMetadataAndCheckIdentityField(InvalidAnnotations.TwoIdsOnSameClass.class.getName());
        });
    }

    @Test
    void shouldRejectNeitherGraphIdOrId() {
        assertThrows(MetadataException.class, () -> {
            createMetadataAndCheckIdentityField(InvalidAnnotations.NeitherGraphIdOrId.class.getName());
        });
    }

    @Test
    void shouldRejectChildHasPrimaryIndexExtendsAndParentHasId() {
        assertThrows(MetadataException.class, () -> {
            metaData = new MetaData(ValidAnnotations.Basic.class.getName(),
                InvalidAnnotations.ChildHasPrimaryIndexExtendsAndParentHasId.class.getName());
        });
    }

    @Test
    void shouldRejectUuidGenerationStrategyWithIdTypeNotUuid() {
        assertThrows(MetadataException.class, () -> {
            createMetadataAndCheckIdentityField(InvalidAnnotations.UuidGenerationStrategyWithIdTypeNotUuid.class.getName());
        });
    }

    @Test
    void shouldRejectGeneratedValueWithoutID() {
        assertThrows(MetadataException.class, () -> {
            createMetadataAndCheckIdentityField(InvalidAnnotations.GeneratedValueWithoutID.class.getName());
        });
    }

    private void createMetadataAndCheckIdentityField(String name) {
        metaData = new MetaData(name);
        metaData.classInfo(name).identityField();
    }

}
