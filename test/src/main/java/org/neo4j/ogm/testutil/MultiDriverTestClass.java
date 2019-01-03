/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.testutil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
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
            Path databaseDirectory = Paths.get(path.toFile().getAbsolutePath() + "/database");
            Files.createDirectories(databaseDirectory);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    FileUtils.deleteDirectory(path);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete temporary files in " + path, e);
                }
            }));
            return path.toFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Use this to limit if the test should execute only on enterprise edition
     * <p>
     * In @BeforeClass or @Before method
     * <p>
     * assumeTrue(isEnterpriseEdition());
     */
    protected static boolean isEnterpriseEdition() {
        // use simple class name here - package differs across versions
        // also removes this class's dependency on enterprise jars
        return getGraphDatabaseService().getClass().getSimpleName().equals("EnterpriseGraphDatabase");
    }

    /**
     * Use this to limit if the test should execute on certain version
     *
     * @param version version, e.g. 3.2.0
     * @return true if the version is equal or greater that given parameter
     */
    protected static boolean isVersionOrGreater(String version) {
        Result result = getGraphDatabaseService().execute("CALL dbms.components()");
        Map<String, Object> kernel = result.stream()
            .filter(map -> map.get("name").equals("Neo4j Kernel"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Expected component named 'Neo4j Kernel'"));
        String kernelVersion = (String) ((List<Object>) kernel.get("versions")).get(0);
        return version.compareTo(kernelVersion) <= 0;
    }
}
