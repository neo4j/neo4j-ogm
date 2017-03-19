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
import org.junit.Assert;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.ogm.config.Components;
import org.neo4j.ogm.drivers.AbstractDriverTestSuite;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;

/**
 * @author vince
 */
public class EmbeddedDriverTest extends AbstractDriverTestSuite {

    private GraphDatabaseService graphDatabaseService;

    @BeforeClass
    public static void configure() throws Exception {
        Components.configure("embedded.driver.properties");
        deleteExistingEmbeddedDatabase();
    }


    @AfterClass
    public static void reset() {
        Components.destroy();
    }

    @Override
    public void setUpTest() {
        graphDatabaseService = ((EmbeddedDriver) Components.driver()).getGraphDatabaseService();
        Assert.assertTrue(graphDatabaseService instanceof GraphDatabaseFacade);
    }

    @Override
    public void tearDownTest() {
    }
}
