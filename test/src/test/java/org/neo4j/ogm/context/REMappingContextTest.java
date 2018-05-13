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

        Object entityById = context.getEntityById(metaData.classInfo(likes), "test-uuid");
        assertThat(entityById).isSameAs(likes);
    }

    @Test
    public void clearContextClearsPrimaryId() throws Exception {
        context.addRelationshipEntity(likes, 1L);

        context.clear();

        Object entity = context.getEntityById(metaData.classInfo(likes), "test-uuid");
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
