/*
 * Copyright (c) 2002-2022 "Neo4j,"
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

import java.time.Duration;
import java.util.function.Supplier;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.connectors.HttpConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilder;
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
    private Neo4j controls;

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

            Supplier<Neo4jBuilder> testHarnessProvider = new TestHarnessSupplier();

            if (enableBolt) {
                this.controls = testHarnessProvider.get()
                    .withConfig(BoltConnector.enabled, true)
                    .withConfig(BoltConnector.listen_address, new SocketAddress("localhost", port))
                    .build();

                this.uri = controls.boltURI().toString();
            } else {
                this.controls = testHarnessProvider.get()
                    .withConfig(HttpConnector.enabled, true)
                    .withConfig(HttpConnector.listen_address, new SocketAddress("localhost", port))
                    .withConfig(GraphDatabaseSettings.transaction_timeout, Duration.ofSeconds(transactionTimeoutSeconds))
                    .build();

                this.uri = controls.httpURI().toString();
            }

            this.database = this.controls.defaultDatabaseService();

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
            controls.databaseManagementService().shutdown();
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
