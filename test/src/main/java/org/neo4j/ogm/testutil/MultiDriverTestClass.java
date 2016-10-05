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

package org.neo4j.ogm.testutil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.service.Components;
import org.neo4j.test.TestGraphDatabaseFactory;


/**
 * @author vince
 */
public class MultiDriverTestClass {


    private static TestServer testServer;
    private static GraphDatabaseService impermanentDb;


    @BeforeClass
    public static synchronized void setupMultiDriverTestEnvironment() {

        Driver driver = Components.driver(); // this will load the driver

        if (driver instanceof HttpDriver ) {
            if (Components.neo4jVersion() < 2.2) {
                testServer = new TestServer.Builder()
                        .enableAuthentication(false)
                        .enableBolt(false)
                        .transactionTimeoutSeconds(2)
                        .build();
            } else {
                testServer = new TestServer.Builder()
                        .enableAuthentication(true)
                        .enableBolt(false)
                        .transactionTimeoutSeconds(2)
                        .build();
            }
        }
        else if (driver instanceof BoltDriver) {
            testServer = new TestServer.Builder()
                    .enableBolt(true)
                    .transactionTimeoutSeconds(2)
                    .build();
        }
        else {
            impermanentDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
            Components.setDriver(new EmbeddedDriver(impermanentDb));
        }

    }

    @AfterClass
    public static synchronized void tearDownMultiDriverTestEnvironment() {
        close();
    }

    private static void close() {

        if (testServer != null) {
            if (testServer.isRunning(100)) {
                testServer.shutdown();
            }
            testServer = null;
        }
        if (impermanentDb != null) {
            if (impermanentDb.isAvailable(100)) {
                impermanentDb.shutdown();
            }
            impermanentDb = null;
        }
    }

    public static synchronized GraphDatabaseService getGraphDatabaseService() {
        if (testServer != null) {
            return testServer.getGraphDatabaseService();
        }
        return impermanentDb;
    }
}