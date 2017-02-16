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

package org.neo4j.ogm.drivers.http;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.ogm.drivers.AbstractDriverTestSuite;
import org.neo4j.ogm.config.Components;
import org.neo4j.ogm.testutil.TestServer;

/**
 * @author vince
 */
public class HttpDriverTest extends AbstractDriverTestSuite {

    private static TestServer testServer;

    @BeforeClass
    public static void configure() {
        Components.configure("ogm-http.properties");
        System.out.println("Http: " + Components.neo4jVersion());
        testServer = new TestServer.Builder().build();
    }

    @AfterClass
    public static void reset() {
        testServer.shutdown();
        Components.destroy();
    }

    @Before
    public void setUpTest() {
    }

    @Override
    public void tearDownTest() {
    }
}
