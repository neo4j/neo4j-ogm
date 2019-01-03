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

import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilder;
import org.neo4j.server.AbstractNeoServer;
import org.neo4j.server.database.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class TestServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestServer.class);

    private final Integer port;
    private final Integer transactionTimeoutSeconds;
    private final Boolean enableAuthentication;
    private final Boolean enableBolt;

    private GraphDatabaseService database;
    private ServerControls controls;

    private String username;
    private String password;
    private String uri;

    public TestServer(boolean enableAuthentication, boolean enableBolt, int transactionTimeoutSeconds) {
        this(enableAuthentication, enableBolt, transactionTimeoutSeconds, TestUtils.getAvailablePort());
    }

    public TestServer(boolean enableAuthentication, boolean enableBolt, int transactionTimeoutSeconds, int port) {

        this.port = port;
        this.transactionTimeoutSeconds = transactionTimeoutSeconds;
        this.enableAuthentication = enableAuthentication;
        this.enableBolt = enableBolt;

        startServer();
        LOGGER.info("Starting {} server on: {}", enableBolt ? "BOLT" : "HTTP", port);
    }

    /**
     * Returns TestServerBuilder based on what is present on classpath
     */
    public static TestServerBuilder newInProcessBuilder() {

        // Use reflection here so there is no compile time dependency on neo4j-harness-enterprise

        TestServerBuilder builder;
        builder = instantiate("org.neo4j.harness.internal.EnterpriseInProcessServerBuilder");
        if (builder == null) {
            // class name for Neo4j 3.1
            builder = instantiate("org.neo4j.harness.EnterpriseInProcessServerBuilder");
        }
        if (builder == null) {
            builder = instantiate("org.neo4j.harness.internal.InProcessServerBuilder");
        }
        /*
         The property "unsupported.dbms.jmx_module.enabled=false" disables JMX monitoring
         We may start multiple instances of the server and without disabling this the 2nd instance would not start.
         */
        builder = builder.withConfig("unsupported.dbms.jmx_module.enabled", "false");
        LOGGER.info("Creating new instance of {}", builder.getClass());
        return builder;
    }

    private static TestServerBuilder instantiate(String className) {
        TestServerBuilder builder = null;
        try {
            builder = ((TestServerBuilder) Class.forName(className)
                .newInstance());
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            LOGGER.trace("Could not load {}", className, e);
        }
        return builder;
    }

    public void startServer() {
        try {

            if (enableBolt) {
                controls = newInProcessBuilder()
                    .withConfig("dbms.connector.bolt.type", "BOLT")
                    .withConfig("dbms.connector.bolt.enabled", "true")
                    .withConfig("dbms.connector.bolt.listen_address", "localhost:" + port)
                    .newServer();
            } else {
                controls = newInProcessBuilder()
                    .withConfig("dbms.connector.http.type", "HTTP")
                    .withConfig("dbms.connector.http.enabled", "true")
                    .withConfig("dbms.connector.http.listen_address", "localhost:" + port)
                    .withConfig("dbms.security.auth_enabled", String.valueOf(enableAuthentication))
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
                    authStoreWriter.write("neo4j:SHA-256,03C9C54BF6EEF1FF3DFEB75403401AA0EBA97860CAC187D6452A1FCF4C63353A,819BDB957119F8DFFF65604C92980A91:");
                }
                this.username = "neo4j";
                this.password = "password";
            }

            return authStore.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initialise(ServerControls controls) throws Exception {
        setDatabase(controls);
        this.uri = url();
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
    public void shutdown() {

        if (database != null && database.isAvailable(100)) {
            LOGGER.info("Stopping {} server on: {}", enableBolt ? "BOLT" : "HTTP", port);
            database.shutdown();
            database = null;
        }
        controls.close();
    }

    /**
     * Waits for a period of time and checks the database availability afterwards
     *
     * @return true if the database is available, false otherwise
     */
    boolean isRunning() {
        return database.isAvailable((long) 1000);
    }

    /**
     * Retrieves the base URL of the Neo4j database server used in the test.
     *
     * @return The URL of the Neo4j test server
     */
    private String url() {

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

    public String getUri() {
        return uri;
    }

    public Integer getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
