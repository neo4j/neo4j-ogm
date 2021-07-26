/*
 * Copyright (c) 2002-2021 "Neo4j,"
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
package org.neo4j.ogm.drivers.embedded.driver;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.Driver;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.support.FileUtils;
import org.neo4j.test.TestDatabaseManagementServiceBuilder;

/**
 * @author Michael J. Simons
 */
public class EmbeddedDriverTest {

    // TODO... Do we want to bring up embedded cc? I surely don't think so
    private static final String NAME_OF_HA_DATABASE_CLASS = "HighlyAvailableGraphDatabase";

    @BeforeClass
    public static void setUp() {
        assumeTrue("ogm-embedded.properties".equals(System.getProperty("ogm.properties")));
    }

    @BeforeClass
    public static void assumeDefaultConfigurationIsDifferentFromCustom() {
        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(new Configuration.Builder().build());
            var r = getValueOfCypherPlanner(driver.unwrap(GraphDatabaseService.class));
            assertThat(r.isEmpty()).isFalse();
            assumeTrue("DEFAULT".equals(r.get("value")));
        }
    }

    @Test
    public void shouldUnwrapEmbeddedDriver() {

        Configuration configuration = new Configuration.Builder().build();
        SessionFactory sessionFactory = new SessionFactory(configuration, Bike.class.getPackage().getName());

        // Neo4j-OGM Driver
        assertThat(sessionFactory.unwrap(EmbeddedDriver.class))
            .isInstanceOf(EmbeddedDriver.class);
        // Underlying embedded instance
        assertThat(sessionFactory.unwrap(GraphDatabaseService.class))
            .isInstanceOf(GraphDatabaseService.class);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(BoltDriver.class));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(Driver.class));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> sessionFactory.unwrap(HttpDriver.class));
    }

    @Test
    public void shouldCreateImpermanentInstanceWhenNoURI() {
        Configuration configuration = new Configuration.Builder().build();

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(configuration);
            assertThat(configuration.getURI()).isNull();
            assertThat(driver.unwrap(GraphDatabaseService.class)).isNotNull();
        }
    }

    @Test
    public void shouldWriteAndRead() {

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(new Configuration.Builder().build());

            GraphDatabaseService databaseService = driver.unwrap(GraphDatabaseService.class);

            try (Transaction tx = databaseService.beginTx()) {
                tx.execute("CREATE (n: Node {name: 'node'})");
                Result r = tx.execute("MATCH (n) RETURN n");
                assertThat(r.hasNext()).isTrue();
                tx.commit();
            }
        }
    }

    @Test
    public void shouldWriteAndReadFromProvidedDatabase() {

        GraphDatabaseService impermanentDatabase = new TestDatabaseManagementServiceBuilder().impermanent().build().database("neo4j");

        try (EmbeddedDriver driver = new EmbeddedDriver(impermanentDatabase, null)) {

            GraphDatabaseService databaseService = driver.unwrap(GraphDatabaseService.class);

            try (Transaction tx = databaseService.beginTx()) {
                tx.execute("CREATE (n: Node {name: 'node'})");
                Result r = tx.execute("MATCH (n) RETURN n");
                assertThat(r.hasNext()).isTrue();
                tx.commit();
            }
        }
    }

    @Test
    public void shouldBeAbleToHaveMultipleInstances() {

        Configuration configuration = new Configuration.Builder().build();

        try (
            EmbeddedDriver driver1 = new EmbeddedDriver();
            EmbeddedDriver driver2 = new EmbeddedDriver()) {
            driver1.configure(configuration);
            driver2.configure(configuration);

            GraphDatabaseService service1 = driver1.unwrap(GraphDatabaseService.class);
            GraphDatabaseService service2 = driver2.unwrap(GraphDatabaseService.class);

            assertThat(service1).isNotNull();
            assertThat(service2).isNotNull();

            // instances should be different
            assertThat(service1 == service2).isFalse();

            // underlying file stores should be different
            assertThat(
                service1.toString().equals(service2.toString()))
                .isFalse();
        }
    }

    @Test
    public void impermanentInstancesShouldNotShareTheSameDatabase() {

        Configuration configuration = new Configuration.Builder().build();

        try (EmbeddedDriver driver1 = new EmbeddedDriver();
            EmbeddedDriver driver2 = new EmbeddedDriver()
        ) {
            driver1.configure(configuration);
            driver2.configure(configuration);

            GraphDatabaseService db1 = driver1.unwrap(GraphDatabaseService.class);
            GraphDatabaseService db2 = driver2.unwrap(GraphDatabaseService.class);

            try (Transaction tx = db1.beginTx()) {
                tx.execute("CREATE (n: Node {name: 'node'})");
                tx.commit();
            }

            try (Transaction tx1 = db1.beginTx(); Transaction tx2 = db2.beginTx()) {

                Result r1 = tx1.execute("MATCH (n) RETURN n");
                Result r2 = tx2.execute("MATCH (n) RETURN n");

                assertThat(r1.hasNext()).isTrue();
                assertThat(r2.hasNext()).isFalse();

                tx1.commit();
                tx2.commit();
            }
        } catch (Exception e) {
            fail("Should not have thrown exception");
        }
    }

    @Test // GH-169
    public void shouldCreateDirectoryIfMissing() throws IOException {
        final String embeddedDir = "/var/tmp/ogmEmbeddedDir";
        final String databaseDir = embeddedDir + "/neo4j";
        Path path = Paths.get(embeddedDir);
        if (Files.exists(path)) {
            FileUtils.deleteDirectory(path);
        }

        Configuration configuration = new Configuration.Builder().uri("file://" + databaseDir).build();

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(configuration);
            assertThat(configuration.getURI()).isEqualTo("file://" + databaseDir);
            assertThat(driver.unwrap(GraphDatabaseService.class)).isNotNull();
            assertThat(Files.exists(path)).isTrue();
        }
        FileUtils.deleteDirectory(path);
    }

    @Test
    public void shouldHandleCustomConfFiles() {

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(new Configuration.Builder().neo4jConfLocation("classpath:custom-neo4j.conf").build());

            assertCustomConfiguration(driver);
        }
    }

    @Test
    public void shouldHandleCustomConfFilesFromOgmProperties() {

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(
                new Configuration.Builder(new ClasspathConfigurationSource("ogm-pointing-to-custom-conf.properties"))
                    .build());

            assertCustomConfiguration(driver);
        }
    }

    @Test
    public void shouldLoadHaBasedOnNeo4ConfFile() {

        assumeTrue(canRunHATests());

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(new Configuration.Builder().neo4jConfLocation("classpath:custom-neo4j-ha.conf").build());

            GraphDatabaseService databaseService = driver.unwrap(GraphDatabaseService.class);
            assertThat(databaseService.getClass().getSimpleName()).isEqualTo(NAME_OF_HA_DATABASE_CLASS);
        }
    }

    private static boolean canRunHATests() {
        try {
            Class.forName("org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory", false,
                EmbeddedDriverTest.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void assertCustomConfiguration(EmbeddedDriver driver) {
        var r = getValueOfCypherPlanner(driver.unwrap(GraphDatabaseService.class));
        assertThat(r.isEmpty()).isFalse();
        assertThat(r.get("value")).isEqualTo("COST");
    }

    private static Map<String, Object> getValueOfCypherPlanner(GraphDatabaseService databaseService) {
        return databaseService.executeTransactionally(""
            + "CALL dbms.listConfig()\n"
            + "YIELD name,  value\n"
            + "WHERE name ='cypher.planner'\n"
            + "RETURN value", Map.of(), r -> r.next()
        );
    }
}
