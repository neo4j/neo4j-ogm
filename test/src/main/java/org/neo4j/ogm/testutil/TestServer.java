/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

import static org.neo4j.string.HexString.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadLocalRandom;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class TestServer {

    private static final String DIGEST_ALGO = "SHA-256";
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
                this.controls = newInProcessBuilder()
                    .withConfig("dbms.connector.bolt.type", "BOLT")
                    .withConfig("dbms.connector.bolt.enabled", "true")
                    .withConfig("dbms.connector.bolt.listen_address", "localhost:" + port)
                    .newServer();

                this.uri = controls.boltURI().toString();
            } else {
                TestServerBuilder builder = newInProcessBuilder()
                    .withConfig("dbms.connector.http.type", "HTTP")
                    .withConfig("dbms.connector.http.enabled", "true")
                    .withConfig("dbms.connector.http.listen_address", "localhost:" + port)
                    .withConfig("dbms.security.auth_enabled", Boolean.toString(enableAuthentication))
                    .withConfig("dbms.security.auth_provider", "native")
                    .withConfig("dbms.transaction_timeout", Integer.toString(transactionTimeoutSeconds))
                    .withConfig("remote_shell_enabled", "false");

                if (enableAuthentication) {
                    this.username = "neo4j";
                    this.password = "password";

                    Path authStore = createAuthStore(this.username, this.password);
                    builder = builder
                        .withConfig("unsupported.dbms.security.auth_store.location",
                            authStore.toAbsolutePath().toString());
                }

                this.controls = builder.newServer();
                this.uri = controls.httpURI().toString();
            }

            this.database = this.controls.graph();

            // ensure we shutdown this server when the JVM terminates, if its not been shutdown by user code
            Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
        } catch (Exception e) {
            throw new RuntimeException("Error starting in-process server", e);
        }
    }

    private static Path createAuthStore(String username, String password) throws IOException {

        // Until 3.4 there's org.neo4j.kernel.impl.security.Credential for both CE and Enterprise.
        // Since 3.5 there's a dedicated SystemGraphCredential for the enterprise edition.
        // We should monitor this for further releasese.
        ThreadLocalRandom random = ThreadLocalRandom.current();

        byte[] saltedPassword = new byte[0];
        byte[] salt = new byte[32];
        random.nextBytes(salt);

        try {
            MessageDigest m = MessageDigest.getInstance(DIGEST_ALGO);
            m.update(salt);
            m.update(password.getBytes(StandardCharsets.UTF_8));
            saltedPassword = m.digest();
        } catch (NoSuchAlgorithmException e) {
        }

        String format = "%s:%s,%s,%s:"; // This is the record format from org.neo4j.server.security.auth.UserSerialization
        String record = String
            .format(format, username, DIGEST_ALGO, encodeHexString(saltedPassword), encodeHexString(salt));

        Path authStore = Files
            .write(Files.createTempFile("neo4j", "credentials"), record.getBytes(StandardCharsets.UTF_8));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Files.deleteIfExists(authStore);
            } catch (IOException e) {
            }
        }));
        return authStore;
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
