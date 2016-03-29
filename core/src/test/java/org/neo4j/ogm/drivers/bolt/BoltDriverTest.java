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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.drivers.AbstractDriverTestSuite;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.service.Components;

/**
 * @author Luanne Misquitta
 * @author Vince Bickers
 */
public class BoltDriverTest extends AbstractDriverTestSuite {

	private static ServerControls neoServer;

	@BeforeClass
	public static void configure() {
		neoServer = TestServerBuilders.newInProcessBuilder()
				.withConfig("dbms.connector.0.enabled", "true")
				.newServer();

		Configuration configuration = Components.configuration();
				configuration.driverConfiguration()
				.setDriverClassName("org.neo4j.ogm.drivers.bolt.driver.BoltDriver")
				.setURI(boltURI())
				.setEncryptionLevel("NONE");

		Components.configure(configuration);
	}

	@AfterClass
	public static void reset() {
		Components.driver().close();
		neoServer.close();
	}
	@Override
	public void setUp() {
		assert Components.driver() instanceof BoltDriver;
	}

	private static String boltURI() {
		try {
			Method boltURI = neoServer.getClass().getDeclaredMethod("boltURI");
			try {
				Object uri = boltURI.invoke(neoServer);
				return uri.toString();

			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

}
