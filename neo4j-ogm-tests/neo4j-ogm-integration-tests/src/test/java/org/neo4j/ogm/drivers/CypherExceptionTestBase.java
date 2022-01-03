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

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.ogm.domain.cypher_exception_test.ConstraintedNode;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * Tests extending from this test base make sure that Neo4j-OGM deals with database exception in a consistent way.
 * Idea is to use the {@link Neo4j} from the test harness to build a session factory in a {@link BeforeClass}
 * method to be returned in {@link #getSessionFactory()}.
 * <br><br>
 * In an ideal world, non of the concrete classes should have any {@link Test} method.
 *
 * @param <T></T> type of driver
 * @author Michael J. Simons
 */
public abstract class CypherExceptionTestBase<T extends Driver> {

    private static final String CONSTRAINT_VIOLATED_MESSAGE_PATTERN
        = "Cypher execution failed with code 'Neo\\.ClientError\\.Schema\\.ConstraintValidationFailed': "
        + "Node(?: |\\(?)\\d\\)? already exists with label `?CONSTRAINTED_NODE`? and "
        + "property (?:`|\")name(?:`|\") ?= ?(?:'test'|\\[test\\])\\.";

    protected static final String DOMAIN_PACKAGE = "org.neo4j.ogm.domain.cypher_exception_test";

    protected static Neo4j serverControls;

    @BeforeClass
    public static void startServer() {
        serverControls = Neo4jBuilders.newInProcessBuilder()
            .withFixture(database -> {
                database.executeTransactionally("MATCH (n:CONSTRAINTED_NODE) DETACH DELETE n");
                database.executeTransactionally("CREATE CONSTRAINT ON (n:CONSTRAINTED_NODE) ASSERT n.name IS UNIQUE");
                database.executeTransactionally("CREATE (n:CONSTRAINTED_NODE {name: 'test'})");
                return null;
            }).build();
    }

    protected abstract SessionFactory getSessionFactory();

    @Test
    public void constraintViolationExceptionShouldBeConsistent() {
        Session session = getSessionFactory().openSession();

        Assertions.assertThatExceptionOfType(CypherException.class).isThrownBy(() -> {
            ConstraintedNode node = new ConstraintedNode("test");
            session.save(node);

        }).withMessageMatching(CONSTRAINT_VIOLATED_MESSAGE_PATTERN);
    }

    @AfterClass
    public static void stopServer() {
        serverControls.close();
    }
}
