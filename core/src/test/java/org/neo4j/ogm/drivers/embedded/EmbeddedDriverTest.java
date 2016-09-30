/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.drivers.AbstractDriverTestSuite;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.service.Components;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * @author vince
 */
public class EmbeddedDriverTest extends AbstractDriverTestSuite {

    private GraphDatabaseService graphDatabaseService;

    @BeforeClass
    public static void configure() {
        Components.configure("ogm-embedded.properties");
        System.out.println("Embedded: " + Components.neo4jVersion());
    }

    @AfterClass
    public static void reset() {
        Components.destroy();
    }

    @Override
    public void setUpTest() {
        graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Components.setDriver(new EmbeddedDriver(graphDatabaseService));
    }

    @Override
    public void tearDownTest() {
        graphDatabaseService.shutdown();
    }
}
