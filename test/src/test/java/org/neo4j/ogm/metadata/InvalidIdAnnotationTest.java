/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

import org.junit.Test;
import org.neo4j.ogm.domain.annotations.ids.ValidAnnotations;
import org.neo4j.ogm.domain.invalid.ids.InvalidAnnotations;
import org.neo4j.ogm.domain.invalid.ids.InvalidAnnotations.GraphIdAndIdWithInternalStrategy;
import org.neo4j.ogm.exception.core.MetadataException;

public class InvalidIdAnnotationTest {

    private MetaData metaData;

    @Test(expected = MetadataException.class)
    public void shouldRejectTwoIdsOnSameClass() throws Exception {
        createMetadataAndCheckIdentityField(InvalidAnnotations.TwoIdsOnSameClass.class.getName());
    }

    @Test(expected = MetadataException.class)
    public void shouldRejectGraphIdAndIdWithInternalStrategy() throws Exception {
        createMetadataAndCheckIdentityField(GraphIdAndIdWithInternalStrategy.class.getName());
    }

    @Test(expected = MetadataException.class)
    public void shouldRejectNeitherGraphIdOrId() throws Exception {
        createMetadataAndCheckIdentityField(InvalidAnnotations.NeitherGraphIdOrId.class.getName());
    }

    @Test(expected = MetadataException.class)
    public void shouldRejectBothIdAndPrimaryIndexOnDifferentProperty() throws Exception {
        createMetadataAndCheckIdentityField(
            InvalidAnnotations.BothIdAndPrimaryIndexOnDifferentProperty.class.getName());
    }

    @Test(expected = MetadataException.class)
    public void shouldRejectChildHasPrimaryIndexExtendsAndParentHasId() throws Exception {
        metaData = new MetaData(ValidAnnotations.Basic.class.getName(),
            InvalidAnnotations.ChildHasPrimaryIndexExtendsAndParentHasId.class.getName());
    }

    @Test(expected = MetadataException.class)
    public void shouldRejectUuidGenerationStrategyWithIdTypeNotUuid() throws Exception {
        createMetadataAndCheckIdentityField(InvalidAnnotations.UuidGenerationStrategyWithIdTypeNotUuid.class.getName());
    }

    @Test(expected = MetadataException.class)
    public void shouldRejectGeneratedValueWithoutID() throws Exception {
        createMetadataAndCheckIdentityField(InvalidAnnotations.GeneratedValueWithoutID.class.getName());
    }

    private void createMetadataAndCheckIdentityField(String name) {
        metaData = new MetaData(name);
        metaData.classInfo(name).identityField();
    }

}
