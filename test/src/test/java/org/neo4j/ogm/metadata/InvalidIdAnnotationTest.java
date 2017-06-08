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

import org.junit.Test;
import org.neo4j.ogm.domain.annotations.ids.ValidAnnotations;
import org.neo4j.ogm.domain.invalid.ids.InvalidAnnotations;
import org.neo4j.ogm.domain.invalid.ids.InvalidAnnotations.GraphIdAndIdWithInternalStrategy;
import org.neo4j.ogm.exception.MetadataException;

import static org.assertj.core.api.Assertions.assertThat;

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
        createMetadataAndCheckIdentityField(InvalidAnnotations.BothIdAndPrimaryIndexOnDifferentProperty.class.getName());
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
