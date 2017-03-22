/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.drivers.embedded;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;

/**
 * @author vince
 * @author Luanne Misquitta
 */
public class EmbeddedDatabaseTest {

    private static Configuration configuration = new Configuration();

    @BeforeClass
    public static void setUp() {
    }

    @Test
    public void shouldCreateImpermanentInstanceWhenNoURI() {

        try (EmbeddedDriver driver = new EmbeddedDriver(configuration)) {
            assertNull(configuration.getURI());
            assertNotNull(driver.getGraphDatabaseService());
        }
    }

    @Test
    public void shouldWriteAndRead() {
        try (EmbeddedDriver driver = new EmbeddedDriver(configuration)) {

            GraphDatabaseService databaseService = driver.getGraphDatabaseService();

            try (Transaction tx = databaseService.beginTx()) {
                databaseService.execute("CREATE (n: Node {name: 'node'})");
                Result r = databaseService.execute("MATCH (n) RETURN n");
                assertTrue(r.hasNext());
                tx.success();
            }
        }
    }

    @Test
    public void shouldBeAbleToHaveMultipleInstances() {

        try (
                EmbeddedDriver driver1 = new EmbeddedDriver(configuration);
                EmbeddedDriver driver2 = new EmbeddedDriver(configuration)) {

            assertNotNull(driver1.getGraphDatabaseService());
            assertNotNull(driver2.getGraphDatabaseService());

            // instances should be different
            assertFalse(driver1.getGraphDatabaseService() == driver2.getGraphDatabaseService());

            // underlying file stores should be different
            assertFalse(driver1.getGraphDatabaseService().toString().equals(driver2.getGraphDatabaseService().toString()));
        }
    }

    @Test
    public void impermanentInstancesShouldNotShareTheSameDatabase() {

        try (EmbeddedDriver driver1 = new EmbeddedDriver(configuration);
             EmbeddedDriver driver2 = new EmbeddedDriver(configuration)
        ) {
            GraphDatabaseService db1 = driver1.getGraphDatabaseService();
            GraphDatabaseService db2 = driver2.getGraphDatabaseService();

            try (Transaction tx = db1.beginTx()) {
                db1.execute("CREATE (n: Node {name: 'node'})");
                tx.success();
            }

            try (Transaction tx1 = db1.beginTx(); Transaction tx2 = db2.beginTx()) {

                Result r1 = db1.execute("MATCH (n) RETURN n");
                Result r2 = db2.execute("MATCH (n) RETURN n");

                assertTrue(r1.hasNext());
                assertFalse(r2.hasNext());

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

        configuration.setURI("file://" + EMBEDDED_DIR);
        try (EmbeddedDriver driver = new EmbeddedDriver(configuration)) {
            assertEquals("file://" + EMBEDDED_DIR, configuration.getURI());
            assertNotNull(driver.getGraphDatabaseService());
            assertTrue(Files.exists(path));
        }
        deleteDirectory(path);
        configuration.setURI(null);
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
