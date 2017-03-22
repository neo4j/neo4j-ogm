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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.ogm.config.Components;
import org.neo4j.ogm.driver.DriverManager;
import org.neo4j.ogm.drivers.AbstractDriverTestSuite;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * @author vince
 */
public class EmbeddedDriverTest extends AbstractDriverTestSuite {

    private static GraphDatabaseService impermanentDb;
    private static File graphStore;

    @BeforeClass
    public static void configure() throws Exception {
        Components.configure("embedded.driver.properties");
        graphStore = createTemporaryGraphStore();
        impermanentDb = new TestGraphDatabaseFactory().newImpermanentDatabase(graphStore);
        DriverManager.register(new EmbeddedDriver(impermanentDb));
    }


    @AfterClass
    public static void reset() {
        if (impermanentDb != null) {
            if (impermanentDb.isAvailable(1000)) {
                impermanentDb.shutdown();
            }
            impermanentDb = null;
            graphStore = null;
        }
    }

    @Override
    public void setUpTest() {

    }


    public static File createTemporaryGraphStore() {
        try {
            Path path = Files.createTempDirectory("graph.db");
            File f = path.toFile();
            f.deleteOnExit();
            return f;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
