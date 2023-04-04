/*
 * Copyright (c) 2002-2023 "Neo4j,"
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

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.neo4j.driver.SessionConfig;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DatabaseSelection;
import org.neo4j.ogm.persistence.types.nativetypes.DatesBoltTest;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Gerrit Meier
 */
@EnabledIfEnvironmentVariable(named = TestContainersTestBase.SYS_PROPERTY_ACCEPT_AND_USE_COMMERCIAL_EDITION, matches = "yes")
public class BoltDatabaseSelectionTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    @BeforeAll
    public static void setupSessionFactoryAndDatabase() throws Exception {

        try (var session = getNewBoltConnection().session()) {
            session.run("CREATE DATABASE anotherDatabase").consume();
            Thread.sleep(500); // we need to wait for the database to get created
        }
        try (var session = getNewBoltConnection().session(SessionConfig.builder().withDatabase("anotherDatabase").build())) {
            session.run("CREATE (n:Node)").consume();
        }

        Configuration ogmConfiguration = getBaseConfigurationBuilder()
            .databaseSelectionProvider(() -> DatabaseSelection.select("anotherDatabase"))
            .build();

        BoltDriver boltOgmDriver = new BoltDriver();
        boltOgmDriver.configure(ogmConfiguration);
        sessionFactory = new SessionFactory(boltOgmDriver, DatesBoltTest.class.getPackage().getName());
    }

    @Test
    public void sessionConfigShouldRespectDatabaseSelection() {
        var result = sessionFactory.openSession()
            .query("MATCH (n:Node) RETURN n", Map.of())
            .queryResults();

        assertThat(result).hasSize(1).allSatisfy(map ->
            assertThat(map).containsKey("n"));

    }

    @AfterAll
    public static void removeDatabase() {
        try (var session = getNewBoltConnection().session()) {
            session.run("DROP DATABASE anotherDatabase").consume();
        } catch (Exception e) {
            // ignore
        }
    }
}
