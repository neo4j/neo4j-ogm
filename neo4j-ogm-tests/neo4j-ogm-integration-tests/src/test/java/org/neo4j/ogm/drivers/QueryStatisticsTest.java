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
package org.neo4j.ogm.drivers;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frantisek Hartman
 */
public class QueryStatisticsTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private static final Logger logger = LoggerFactory.getLogger(QueryStatisticsTest.class);

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.social");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void statisticsContainsUpdates() {
        Result result = session.query("CREATE (n:Node)", emptyMap());
        QueryStatistics statistics = result.queryStatistics();

        assertThat(statistics.containsUpdates()).isTrue();
    }

    @Test
    public void statisticsNodesCreated() {
        Result result = session.query("CREATE (n:Node)", emptyMap());
        QueryStatistics statistics = result.queryStatistics();

        assertThat(statistics.getNodesCreated()).isEqualTo(1);
    }

    @Test
    public void statisticsNodesDeleted() {
        session.query("CREATE (n:Node)", emptyMap());

        Result result = session.query("MATCH (n:Node) DELETE n", emptyMap());
        QueryStatistics statistics = result.queryStatistics();

        assertThat(statistics.getNodesDeleted()).isEqualTo(1);
    }

    @Test
    public void statisticsPropertiesSet() {
        Result result = session.query("CREATE (n:Node {name:'Frantisek'})-[r:REL {weight:1.0}]->(n2:Node)", emptyMap());
        QueryStatistics statistics = result.queryStatistics();

        assertThat(statistics.getPropertiesSet()).isEqualTo(2);
    }

    @Test
    public void statisticsRelationshipsCreated() {
        Result result = session.query("CREATE (n:Node)-[r:REL]->(n2:Node)", emptyMap());
        QueryStatistics statistics = result.queryStatistics();

        assertThat(statistics.getRelationshipsCreated()).isEqualTo(1);
    }

    @Test
    public void statisticsRelationshipsDeleted() {
        session.query("CREATE (n:Node)-[r:REL]->(n2:Node)", emptyMap());

        Result result = session.query("MATCH (n:Node)-[r:REL]->(n2:Node) DELETE r", emptyMap());
        QueryStatistics statistics = result.queryStatistics();

        assertThat(statistics.getRelationshipsDeleted()).isEqualTo(1);
    }

    @Test
    public void statisticsLabelsAdded() {
        Result result = session.query("CREATE (n:Node)", emptyMap());
        QueryStatistics statistics = result.queryStatistics();

        assertThat(statistics.getLabelsAdded()).isEqualTo(1);
    }

    @Test
    public void statisticsLabelsRemoved() {
        session.query("CREATE (n:Node)", emptyMap());
        Result result = session.query("MATCH (n:Node) REMOVE n:Node", emptyMap());
        QueryStatistics statistics = result.queryStatistics();

        assertThat(statistics.getLabelsRemoved()).isEqualTo(1);
    }

    @Test
    public void statisticsIndexesAdded() {
        try {
            Result result = session.query("CREATE INDEX foo FOR (n:Label) ON (n.property)", emptyMap());
            QueryStatistics statistics = result.queryStatistics();

            assertThat(statistics.getIndexesAdded()).isEqualTo(1);
        } catch (Exception e) {
            fail("Error during test", e);
        } finally {

            // try to drop the index so other tests have clean db
            try {
                session.query("DROP INDEX foo", emptyMap());
            } catch (Exception e1) {
                logger.warn("Error during index/constraint cleanup", e1);
            }
        }
    }

    @Test
    public void statisticsIndexesRemoved() {
        try {
            session.query("CREATE INDEX foo FOR (n:Label) ON (n.property)", emptyMap());

            Result result = session.query("DROP INDEX foo", emptyMap());
            QueryStatistics statistics = result.queryStatistics();

            assertThat(statistics.getIndexesRemoved()).isEqualTo(1);
        } catch (Exception e) {
            fail("Error during test", e);
        } finally {

            // try to drop the index so other tests have clean db
            try {
                session.query("DROP INDEX foo IF EXISTS", emptyMap());
                // expected - normally this test will remove it, here just in case something fails
            } catch (Exception e1) {
                logger.warn("Error during index/constraint cleanup", e1);
            }
        }
    }

    @Test
    public void statisticsConstraintsAdded() {
        try {
            Result result = session.query("CREATE CONSTRAINT foo FOR (n:Node) REQUIRE n.property IS UNIQUE", emptyMap());
            QueryStatistics statistics = result.queryStatistics();

            assertThat(statistics.getConstraintsAdded()).isEqualTo(1);
        } catch (Exception e) {
            fail("Error during test", e);
        } finally {

            // try to drop the index so other tests have clean db
            try {
                session.query("DROP CONSTRAINT foo", emptyMap());
            } catch (Exception e1) {
                logger.warn("Error during index/constraint cleanup", e1);
            }
        }
    }

    @Test
    public void statisticsConstraintsRemoved() {
        try {
            session.query("CREATE CONSTRAINT foo FOR (n:Node) REQUIRE n.property IS UNIQUE", emptyMap());

            Result result = session.query("DROP CONSTRAINT foo", emptyMap());
            QueryStatistics statistics = result.queryStatistics();

            assertThat(statistics.getConstraintsRemoved()).isEqualTo(1);
        } catch (Exception e) {
            fail("Error during test", e);
        } finally {

            // try to drop the index so other tests have clean db
            try {
                session.query("DROP CONSTRAINT foo IF EXISTS", emptyMap());
            } catch (Exception e1) {
                // expected - normally this test will remove it, here just in case something fails
                logger.debug("Error during index/constraint cleanup", e1);
            }
        }
    }

}
