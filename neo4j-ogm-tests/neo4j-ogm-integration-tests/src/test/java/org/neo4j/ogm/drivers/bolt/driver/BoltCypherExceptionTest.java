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
package org.neo4j.ogm.drivers.bolt.driver;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Driver;
import org.neo4j.ogm.domain.cypher_exception_test.ConstraintedNode;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Michael J. Simons
 */
public class BoltCypherExceptionTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private static final String CONSTRAINT_VIOLATED_MESSAGE_PATTERN
        = "Cypher execution failed with code 'Neo.ClientError.Schema.ConstraintValidationFailed': ";

    protected static final String DOMAIN_PACKAGE = "org.neo4j.ogm.domain.cypher_exception_test";

    @BeforeAll
    public static void startServer() {
        sessionFactory = new SessionFactory(getDriver(), DOMAIN_PACKAGE);

        try (var session = getDriver().unwrap(Driver.class).session()) {
            session.run("MATCH (n:CONSTRAINTED_NODE) DETACH DELETE n").consume();
            session.run("CREATE CONSTRAINT BLUBB IF NOT EXISTS FOR (n:CONSTRAINTED_NODE) REQUIRE n.name IS UNIQUE").consume();
            session.run("CREATE (n:CONSTRAINTED_NODE {name: 'test'})").consume();
        }
    }

    @Test
    void constraintViolationExceptionShouldBeConsistent() {
        Session session = sessionFactory.openSession();

        Assertions.assertThatExceptionOfType(CypherException.class).isThrownBy(() -> {
            ConstraintedNode node = new ConstraintedNode("test");
            session.save(node);

        }).withMessageStartingWith(CONSTRAINT_VIOLATED_MESSAGE_PATTERN);
    }

    @AfterAll
    public static void removeDataAndCloseSessionFactory() {
        try (var session = getDriver().unwrap(Driver.class).session()) {
            session.run("MATCH (n) DETACH DELETE n").consume();
        }
        sessionFactory.close();
    }
}
