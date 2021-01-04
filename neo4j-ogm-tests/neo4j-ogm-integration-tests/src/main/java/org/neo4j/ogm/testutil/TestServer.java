/*
 * Copyright (c) 2002-2021 "Neo4j,"
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

import java.util.function.Supplier;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(TestServer.class);

    private final Integer port;
    private final Integer transactionTimeoutSeconds;
    private final Boolean enableBolt;

    private GraphDatabaseService database;
    private ServerControls controls;

    private String uri;

    public TestServer(boolean enableBolt, int transactionTimeoutSeconds) {
        this(enableBolt, transactionTimeoutSeconds, TestUtils.getAvailablePort());
    }

    public TestServer(boolean enableBolt, int transactionTimeoutSeconds, int port) {

        this.port = port;
        this.transactionTimeoutSeconds = transactionTimeoutSeconds;
        this.enableBolt = enableBolt;

        startServer();
        LOGGER.info("Starting {} server on: {}", enableBolt ? "BOLT" : "HTTP", port);
    }

    public void startServer() {
        try {

            Supplier<TestServerBuilder> testHarnessProvider = new TestHarnessSupplier();

            if (enableBolt) {
                this.controls = testHarnessProvider.get()
                    .withConfig("dbms.connector.bolt.type", "BOLT")
                    .withConfig("dbms.connector.bolt.enabled", "true")
                    .withConfig("dbms.connector.bolt.listen_address", "localhost:" + port)
                    .newServer();

                this.uri = controls.boltURI().toString();
            } else {
                TestServerBuilder builder = testHarnessProvider.get()
                    .withConfig("dbms.connector.http.type", "HTTP")
                    .withConfig("dbms.connector.http.enabled", "true")
                    .withConfig("dbms.connector.http.listen_address", "localhost:" + port)
                    .withConfig("dbms.transaction_timeout", Integer.toString(transactionTimeoutSeconds))
                    .withConfig("remote_shell_enabled", "false");

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

    public String getUri() {
        return uri;
    }

    public Integer getPort() {
        return port;
    }
}
