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

package org.neo4j.ogm.drivers.bolt;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.ogm.drivers.AbstractDriverTestSuite;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.testutil.TestServer;

import static org.junit.Assume.assumeTrue;

import static org.neo4j.ogm.config.DriverConfiguration.URI;
import static org.neo4j.ogm.config.DriverConfiguration.URIS;

/**
 * @author Frantisek Hartman
 */
public class MultipleURIsBoltDriverTest extends AbstractDriverTestSuite {

	private static final Logger logger = LoggerFactory.getLogger(MultipleURIsBoltDriverTest.class);


	private static TestServer testServer;

	@BeforeClass
	public static void configure() {
		Components.configure("ogm-bolt.properties");


		System.out.println("Bolt: " + Components.neo4jVersion());
		if (Components.neo4jVersion() >= 3.0) {
			testServer = new TestServer.Builder().enableBolt(true).build();
		}

		// This is no ideal but we are constrained by the Components static infrastructure.
		logger.warn("Expected exception about not being able to connect to localhost:1023 should follow");
		// Take the uri in config - it is set to the local test server on random port
		Object uri = Components.getConfiguration().get(URI);
		// port 1023 will be free (needs root), it is the not working uri
		Components.getConfiguration().set(URI[0], "bolt+routing://localhost:1023");
		// set the working uri to additional URIS
		Components.getConfiguration().set(URIS[0], uri);
		Components.driver().configure(Components.getConfiguration().driverConfiguration());
	}

	@AfterClass
	public static void reset() {
		if (Components.neo4jVersion() >= 3.0) {
			testServer.shutdown();
		}
		Components.destroy();
	}

	@Override
	public void setUpTest() {
		assumeTrue(Components.neo4jVersion() >= 3.0);
	}

	@Override
	public void tearDownTest() {
	}
}
