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

import static org.neo4j.ogm.config.Components.configure;

import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.config.Components;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.DriverManager;
import org.neo4j.server.AbstractNeoServer;
import org.neo4j.server.database.Database;

/**
 * @author Vince Bickers
 */
@SuppressWarnings("deprecation")
public class TestServer {

    private final Integer port;
    private final Integer transactionTimeoutSeconds;
    private final Boolean enableAuthentication;
    private final Boolean enableBolt;
    private final Configuration configuration;

    private GraphDatabaseService database;
    private ServerControls controls;


    private TestServer(Builder builder) {

        this.configuration = builder.configuration;
        this.port = builder.port == null ? TestUtils.getAvailablePort() : builder.port;
        this.transactionTimeoutSeconds = builder.transactionTimeoutSeconds;
        this.enableAuthentication = builder.enableAuthentication;
        this.enableBolt = builder.enableBolt;

        startServer();

        System.out.println("* Starting new in memory test server on: " + url());
    }

    private void startServer() {
        try {

            if (enableBolt) {
                controls = TestServerBuilders.newInProcessBuilder()
                        .withConfig("dbms.connector.0.enabled", "true")
                        .withConfig("dbms.connector.0.address", "localhost:" + String.valueOf(port))
                        .newServer();
            } else {
                controls = TestServerBuilders.newInProcessBuilder()
                        .withConfig("dbms.connector.1.enabled", "true")
                        .withConfig("dbms.connector.1.address", "localhost:" + String.valueOf(port))
                        .withConfig("dbms.security.auth_enabled", String.valueOf(enableAuthentication))
                        .withConfig("org.neo4j.server.webserver.port", String.valueOf(port))
                        .withConfig("org.neo4j.server.transaction.timeout", String.valueOf(transactionTimeoutSeconds))
                        .withConfig("dbms.transaction_timeout", String.valueOf(transactionTimeoutSeconds))
                        .withConfig("dbms.security.auth_store.location", createAuthStore())
                        .withConfig("unsupported.dbms.security.auth_store.location", createAuthStore())
                        .withConfig("remote_shell_enabled", "false")
                        .newServer();
            }

            initialise(controls);

            // ensure we shutdown this server when the JVM terminates, if its not been shutdown by user code
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        } catch (Exception e) {
            throw new RuntimeException("Error starting in-process server", e);
        }
    }

    private String createAuthStore() {
        // creates a temp auth store, with encrypted credentials "neo4j:password" if the server is authenticating connections
        try {
            Path authStore = Files.createTempFile("neo4j", "credentials");
            authStore.toFile().deleteOnExit();

            if (enableAuthentication) {
                try (Writer authStoreWriter = new FileWriter(authStore.toFile())) {
                    IOUtils.write("neo4j:SHA-256,03C9C54BF6EEF1FF3DFEB75403401AA0EBA97860CAC187D6452A1FCF4C63353A,819BDB957119F8DFFF65604C92980A91:", authStoreWriter);
                }
                configuration.setCredentials("neo4j", "password");
            }

            return authStore.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initialise(ServerControls controls) throws Exception {
        setDatabase(controls);
        configuration.setURI(url());
        DriverManager.register(configuration.getDriverClassName());
        DriverManager.getDriver().configure(configuration); // we must reconfigure the driver if the URL has changed
    }

    private void setDatabase(ServerControls controls) throws Exception {
        try {
            Method method = controls.getClass().getMethod("graph");
            database = (GraphDatabaseService) method.invoke(controls);
        } catch (NoSuchMethodException nsme) {
            Class clazz = Class.forName("org.neo4j.harness.internal.InProcessServerControls");
            Field field = clazz.getDeclaredField("server");
            field.setAccessible(true);
            AbstractNeoServer server = (AbstractNeoServer) field.get(controls);
            Database db = server.getDatabase();
            database = db.getGraph();
        }
    }

    /**
     * Stops the underlying server bootstrapper and, in turn, the Neo4j server.
     */
    public synchronized void shutdown() {

        if (database != null && database.isAvailable(100)) {
            System.out.println("* Stopping in memory test server on: " + url());
            database.shutdown();
            database = null;
        }
        controls.close();
    }

    /**
     * Waits for a period of time and checks the database availability afterwards
     *
     * @param timeout milliseconds to wait
     * @return true if the database is available, false otherwise
     */
    public boolean isRunning(long timeout) {
        return database.isAvailable(timeout);
    }

    /**
     * Retrieves the base URL of the Neo4j database server used in the test.
     *
     * @return The URL of the Neo4j test server
     */
    public String url() {

        Method method;
        try {
            if (enableBolt) {
                method = controls.getClass().getMethod("boltURI");
            } else {
                method = controls.getClass().getMethod("httpURI");
            }
            Object url = method.invoke(controls);
            return url.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the underlying {@link org.neo4j.graphdb.GraphDatabaseService} used in this test.
     *
     * @return The test {@link org.neo4j.graphdb.GraphDatabaseService}
     */
    public GraphDatabaseService getGraphDatabaseService() {
        return this.database;
    }

    public static class Builder {

        private Integer port = null;
        private Integer transactionTimeoutSeconds = 60;
        private boolean enableAuthentication = false;
        private boolean enableBolt = false;
        private Configuration configuration;

        public Builder(Configuration configuration) {
            this.configuration = configuration;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder transactionTimeoutSeconds(int transactionTimeoutSeconds) {
            this.transactionTimeoutSeconds = transactionTimeoutSeconds;
            return this;
        }

        public Builder enableAuthentication(boolean enableAuthentication) {
            this.enableAuthentication = enableAuthentication;
            return this;
        }

        public Builder enableBolt(boolean enableBolt) {
            this.enableBolt = enableBolt;
            return this;
        }

        public TestServer build() {
            return new TestServer(this);
        }
    }
}
