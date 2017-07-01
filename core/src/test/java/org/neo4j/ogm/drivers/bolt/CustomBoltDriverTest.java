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

import java.util.logging.Level;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.driver.internal.logging.ConsoleLogging;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.ogm.drivers.AbstractDriverTestSuite;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.testutil.TestServer;

import static org.junit.Assume.assumeTrue;

/**
 * @author Luanne Misquitta
 * @author Vince Bickers
 */
public class CustomBoltDriverTest extends AbstractDriverTestSuite {

	private static TestServer testServer;

	@BeforeClass
	public static void configure() {
		Components.configure("ogm-bolt.properties");
		
		System.out.println("Bolt: " + Components.neo4jVersion());
		if (Components.neo4jVersion() >= 3.0) {
			testServer = new TestServer.Builder().enableBolt(true).build();
		}

		Config config = Config.build().withLogging(new ConsoleLogging(Level.FINE)).toConfig();
		Driver driver = GraphDatabase.driver(testServer.url(), config);
		Components.setDriver(new BoltDriver(driver));
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
