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
import static org.assertj.core.api.Assumptions.*;

import java.util.Collections;

import org.assertj.core.api.Assumptions;
import org.junit.Test;
import org.neo4j.ogm.config.AutoIndexMode;
import org.neo4j.ogm.domain.autoindex.SingleIndexEntity;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 * @author Gerrit Meier
 */
public class SingleIndexAutoIndexManagerTest extends BaseAutoIndexManagerTestClass {

    private static final String INDEX = "INDEX org_neo4j_ogm_domain_autoindex_singleindexentity_login FOR (`entity`:`Entity`) ON (`entity`.`login`)";
    private static final String CONSTRAINT = "CONSTRAINT org_neo4j_ogm_domain_autoindex_singleindexentity_login FOR (entity:Entity) REQUIRE entity.login IS UNIQUE";

    public SingleIndexAutoIndexManagerTest() {
        super(new String[] { INDEX }, SingleIndexEntity.class);
    }

    @Override
    protected void additionalTearDown() {
        executeDrop(CONSTRAINT);
        executeDrop(INDEX);
    }

    @Test
    public void testAutoIndexManagerUpdateConstraintChangedToIndex() {
        executeCreate(CONSTRAINT);

        runAutoIndex("update");

        executeForIndexes(indexes -> assertThat(indexes).hasSize(1 + expectedNumberOfAdditionalIndexes));
        executeForConstraints(constraints -> assertThat(constraints).isEmpty());
    }

    @Test
    public void lookupIndexesMustNotBeDropped() {

        assumeThat(isVersionOrGreater("4.3.0"))
            .withFailMessage("This test can only run on Neo4j 4.3+")
            .isTrue();
        sessionFactory.openSession().query("CREATE (n:JustSoThatALookupIsCreated) RETURN n", Collections.emptyMap());

        assertThat(getLookupIndexesCount()).isEqualTo(2L);

        runAutoIndex(AutoIndexMode.ASSERT.getName());
        executeForIndexes(indexes -> assertThat(indexes).hasSize(1 + expectedNumberOfAdditionalIndexes));
        executeForConstraints(constraints -> assertThat(constraints).isEmpty());

        assertThat(getLookupIndexesCount()).isEqualTo(2L);
    }

    private long getLookupIndexesCount() {

        Session session = sessionFactory.openSession();
        try (Transaction tx = session.beginTransaction()) {
            return session.queryForObject(Long.class, "call db.indexes() yield type where type = 'LOOKUP' RETURN count(*)", Collections.emptyMap());
        }
    }
}
