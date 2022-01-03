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
