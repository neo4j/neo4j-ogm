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
import org.neo4j.kernel.enterprise.EnterpriseGraphDatabase;

import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.exception.core.ConfigurationException;
import org.neo4j.ogm.session.SessionFactory;


/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class MultiDriverTestClass {

	private static TestServer testServer;
	private static File graphStore;
	private static Configuration.Builder baseConfiguration;
	protected static SessionFactory sessionFactory;
	protected static Driver driver;

	static {

        String configFileName = System.getenv("ogm.properties");
        if (configFileName == null) {
            configFileName = System.getProperty("ogm.properties");
        }
        if (configFileName == null) {
            configFileName = "ogm.properties";
        }
        baseConfiguration = new Configuration.Builder(new ClasspathConfigurationSource(configFileName));

        if (baseConfiguration.build().getDriverClassName().equals(HttpDriver.class.getCanonicalName())) {
            testServer = new TestServer(true, false, 5);
        } else if (baseConfiguration.build().getDriverClassName().equals(BoltDriver.class.getCanonicalName())) {
            testServer = new TestServer(true, true, 5);
        } else {
            graphStore = createTemporaryGraphStore();
        }
    }

	@BeforeClass
	public static void setupMultiDriverTestEnvironment() {

		if (baseConfiguration.build().getDriverClassName().equals(EmbeddedDriver.class.getCanonicalName())) {
            baseConfiguration.uri(graphStore.toURI().toString()).build();
        } else {
            baseConfiguration.uri(testServer.getUri()).credentials(testServer.getUsername(), testServer.getPassword());
		}

		if (driver == null) {
			Configuration configuration = getBaseConfiguration().build();
			driver = newDriverInstance(configuration.getDriverClassName());
			driver.configure(configuration);
		}
	}

	private static Driver newDriverInstance(String driverClassName) {
		try {
			final Class<?> driverClass = Class.forName(driverClassName);
			return (Driver) driverClass.newInstance();
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
			throw new ConfigurationException("Could not load driver class " + driverClassName, e);
		}
	}

	public static Configuration.Builder getBaseConfiguration() {
		return Configuration.Builder.copy(baseConfiguration);
	}

	public static GraphDatabaseService getGraphDatabaseService() {
		// if using an embedded config, return the db from the driver
		if (baseConfiguration.build().getURI().startsWith("file")) {
			if (driver != null) {
				return ((EmbeddedDriver) driver).getGraphDatabaseService();
			} else if (sessionFactory != null) {
				return ((EmbeddedDriver) sessionFactory.getDriver()).getGraphDatabaseService();
			}
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

    /**
     * Use this to limit if the test should execute only on enterprise edition
     *
     * In @BeforeClass or @Before method
     *
     * assumeTrue(isEnterpriseEdition());
     */
	protected static boolean isEnterpriseEdition() {
        return getGraphDatabaseService() instanceof EnterpriseGraphDatabase;
    }
}
