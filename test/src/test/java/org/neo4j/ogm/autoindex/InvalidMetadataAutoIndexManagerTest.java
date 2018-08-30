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

package org.neo4j.ogm.autoindex;

import org.junit.Test;
import org.neo4j.ogm.domain.autoindex.NoPropertyCompositeIndexEntity;
import org.neo4j.ogm.domain.autoindex.invalid.WrongPropertyCompositeIndexEntity;
import org.neo4j.ogm.exception.core.MetadataException;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MetaData;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class InvalidMetadataAutoIndexManagerTest {

    @Test(expected = MetadataException.class)
    public void shouldCheckPropertiesMatchFieldNames() {
        MetaData metadata = new MetaData(WrongPropertyCompositeIndexEntity.class.getName());
        ClassInfo classInfo = metadata.classInfo(WrongPropertyCompositeIndexEntity.class.getName());
        classInfo.getCompositeIndexes();
    }

    @Test(expected = MetadataException.class)
    public void shouldCheckPropertiesExistsForCompositeIndex() {
        MetaData metadata = new MetaData(NoPropertyCompositeIndexEntity.class.getName());
        ClassInfo classInfo = metadata.classInfo(NoPropertyCompositeIndexEntity.class.getName());
        classInfo.getCompositeIndexes();
    }
}
