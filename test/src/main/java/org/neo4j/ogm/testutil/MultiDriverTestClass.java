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

package org.neo4j.ogm.testutil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.DriverManager;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;


/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class MultiDriverTestClass {

	private static TestServer testServer;
	private static File graphStore;
	private static Configuration.Builder baseConfiguration = new Configuration.Builder(new ClasspathConfigurationSource("ogm.properties"));

	static {
		testServer = new TestServer(true, true, 5);
		graphStore = createTemporaryGraphStore();
	}

	@BeforeClass
	public static void setupMultiDriverTestEnvironment() {

		if (baseConfiguration.build().getDriverClassName().equals(HttpDriver.class.getCanonicalName())) {
			baseConfiguration.uri(testServer.getUri()).credentials(testServer.getUsername(), testServer.getPassword());
		} else if (baseConfiguration.build().getDriverClassName().equals(BoltDriver.class.getCanonicalName())) {
			baseConfiguration.uri(testServer.getUri()).credentials(testServer.getUsername(), testServer.getPassword());
		} else {
			baseConfiguration.uri(graphStore.toURI().toString()).build();
		}
	}

	public static Configuration.Builder getBaseConfiguration() {
		return Configuration.Builder.copy(baseConfiguration);
	}

	public static GraphDatabaseService getGraphDatabaseService() {
		// if using an embedded config, return the db from the driver
		if (baseConfiguration.build().getURI().startsWith("file")) {
			return ((EmbeddedDriver) DriverManager.getDriver()).getGraphDatabaseService();
		}
		// else (bolt, http), return just a test server (not really used except for indices ?)
		return testServer.getGraphDatabaseService();
	}

	private static File createTemporaryGraphStore() {
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
