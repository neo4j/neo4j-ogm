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
package org.neo4j.ogm.context;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.metadata.MetaData;

/**
 * Test for relationship entities and mapping context
 */
public class REMappingContextTest {

    private final MetaData metaData = new MetaData("org.neo4j.ogm.context");
    private final MappingContext context = new MappingContext(metaData);

    private Likes likes;

    @Before
    public void setUp() throws Exception {
        likes = new Likes();
        likes.id = 1L;
        likes.uuid = "test-uuid";
    }

    @Test
    public void addedRelationshipEntityIsInContext() throws Exception {
        context.addRelationshipEntity(likes, 1L);

        Object entity = context.getRelationshipEntity(1L);
        assertThat(entity).isSameAs(likes);

        Object entityById = context.getRelationshipEntityById(metaData.classInfo(likes), "test-uuid");
        assertThat(entityById).isSameAs(likes);
    }

    @Test
    public void clearContextClearsPrimaryId() throws Exception {
        context.addRelationshipEntity(likes, 1L);

        context.clear();

        Object entity = context.getRelationshipEntityById(metaData.classInfo(likes), "test-uuid");
        assertThat(entity).isNull();
    }

    @NodeEntity
    static class User {

        Long id;

    }

    @RelationshipEntity(type = "LIKES")
    static class Likes {

        Long id;

        @Id
        String uuid;

        @StartNode
        User from;

        @EndNode
        User to;

        int stars;

    }
}
