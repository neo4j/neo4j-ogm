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

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.neo4j.ogm.domain.autoindex.RelationshipEntityWithSameType1;
import org.neo4j.ogm.domain.autoindex.RelationshipEntityWithSameType2;

/**
 * @author Gerrit Meier
 */
public class DuplicatedConstraintAutoIndexManagerTest extends BaseAutoIndexManagerTestClass {

    private static final String INDEX = "INDEX org_neo4j_ogm_domain_autoindex_relationshipentitywithsametype2 FOR (n:`SAME_TYPE`) ON (n.`id`)";
    private static final String CONSTRAINT = "CONSTRAINT org_neo4j_ogm_domain_autoindex_relationshipentitywithsametype2_id_unique FOR (`same_type`:`SAME_TYPE`) REQUIRE `same_type`.`id` IS UNIQUE";

    public DuplicatedConstraintAutoIndexManagerTest() {
        super(new String[] { CONSTRAINT }, RelationshipEntityWithSameType1.class, RelationshipEntityWithSameType2.class);
    }

    @Override
    protected void additionalTearDown() {
        executeDrop(CONSTRAINT);
        executeDrop(INDEX);
    }

    @Test
    public void shouldNotFailOnDuplicatedConstraint() {

        runAutoIndex("update");

        executeForIndexes(indexes -> assertThat(indexes).hasSize(expectedNumberOfAdditionalIndexes));
        executeForConstraints(constraints -> assertThat(constraints).hasSize(1));
    }
}
