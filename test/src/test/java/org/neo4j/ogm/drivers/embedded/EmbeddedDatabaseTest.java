/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.drivers.embedded;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * @author vince
 * @author Luanne Misquitta
 */
public class EmbeddedDatabaseTest {

    @BeforeClass
    public static void setUp() {
        assumeTrue("ogm-embedded.properties".equals(System.getProperty("ogm.properties")));
    }

    @Test
    public void shouldCreateImpermanentInstanceWhenNoURI() {
        Configuration configuration = new Configuration.Builder().build();

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(configuration);
            assertThat(configuration.getURI()).isNull();
            assertThat(driver.getGraphDatabaseService()).isNotNull();
        }
    }

    @Test
    public void shouldWriteAndRead() {

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(new Configuration.Builder().build());

            GraphDatabaseService databaseService = driver.getGraphDatabaseService();

            try (Transaction tx = databaseService.beginTx()) {
                databaseService.execute("CREATE (n: Node {name: 'node'})");
                Result r = databaseService.execute("MATCH (n) RETURN n");
                assertThat(r.hasNext()).isTrue();
                tx.success();
            }
        }
    }

    @Test
    public void shouldWriteAndReadFromProvidedDatabase() throws Exception {

        GraphDatabaseService impermanentDatabase = new TestGraphDatabaseFactory().newImpermanentDatabase();

        try (EmbeddedDriver driver = new EmbeddedDriver(impermanentDatabase)) {

            GraphDatabaseService databaseService = driver.getGraphDatabaseService();

            try (Transaction tx = databaseService.beginTx()) {
                databaseService.execute("CREATE (n: Node {name: 'node'})");
                Result r = databaseService.execute("MATCH (n) RETURN n");
                assertThat(r.hasNext()).isTrue();
                tx.success();
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

            assertThat(driver1.getGraphDatabaseService()).isNotNull();
            assertThat(driver2.getGraphDatabaseService()).isNotNull();

            // instances should be different
            assertThat(driver1.getGraphDatabaseService() == driver2.getGraphDatabaseService()).isFalse();

            // underlying file stores should be different
            assertThat(
                driver1.getGraphDatabaseService().toString().equals(driver2.getGraphDatabaseService().toString()))
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

            GraphDatabaseService db1 = driver1.getGraphDatabaseService();
            GraphDatabaseService db2 = driver2.getGraphDatabaseService();

            try (Transaction tx = db1.beginTx()) {
                db1.execute("CREATE (n: Node {name: 'node'})");
                tx.success();
            }

            try (Transaction tx1 = db1.beginTx(); Transaction tx2 = db2.beginTx()) {

                Result r1 = db1.execute("MATCH (n) RETURN n");
                Result r2 = db2.execute("MATCH (n) RETURN n");

                assertThat(r1.hasNext()).isTrue();
                assertThat(r2.hasNext()).isFalse();

                tx1.success();
                tx2.success();
            }
        } catch (Exception e) {
            fail("Should not have thrown exception");
        }
    }

    /**
     * @see Issue 169
     */
    @Test
    public void shouldCreateDirectoryIfMissing() throws IOException {
        final String EMBEDDED_DIR = "/var/tmp/ogmEmbeddedDir";
        Path path = Paths.get(EMBEDDED_DIR);
        if (Files.exists(path)) {
            deleteDirectory(path);
        }

        Configuration configuration = new Configuration.Builder().uri("file://" + EMBEDDED_DIR).build();

        try (EmbeddedDriver driver = new EmbeddedDriver()) {
            driver.configure(configuration);
            assertThat(configuration.getURI()).isEqualTo("file://" + EMBEDDED_DIR);
            assertThat(driver.getGraphDatabaseService()).isNotNull();
            assertThat(Files.exists(path)).isTrue();
        }
        deleteDirectory(path);
    }

    private void deleteDirectory(Path path) {
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
