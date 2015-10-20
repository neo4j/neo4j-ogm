/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.core.testutil;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.harness.internal.InProcessServerControls;
import org.neo4j.ogm.api.config.Configuration;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.request.Request;
import org.neo4j.ogm.api.transaction.Transaction;
import org.neo4j.ogm.api.transaction.TransactionManager;
import org.neo4j.ogm.core.spi.Components;
import org.neo4j.server.AbstractNeoServer;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Vince Bickers
 */
@SuppressWarnings("deprecation")
public class TestServer implements Driver {

    private AbstractNeoServer server;
    private GraphDatabaseService database;
    private ServerControls controls;
    private Driver driver;

    public TestServer() {

        try {
            controls = TestServerBuilders.newInProcessBuilder()
                    .withConfig("dbms.security.auth_enabled", String.valueOf(enableAuthentication()))
                    .withConfig("org.neo4j.server.webserver.port", String.valueOf(TestUtils.getAvailablePort()))
                    .withConfig("dbms.security.auth_store.location", authStoreLocation())
                    .newServer();

            initialise(controls);

        } catch (Exception e) {
            throw new RuntimeException("Error starting in-process server",e);
        }

    }

    public TestServer(int port) {

        try {
            controls = TestServerBuilders.newInProcessBuilder()
                    .withConfig("dbms.security.auth_enabled", String.valueOf(enableAuthentication()))
                    .withConfig("org.neo4j.server.webserver.port", String.valueOf(port))
                    .withConfig("dbms.security.auth_store.location", authStoreLocation())
                    .newServer();

            initialise(controls);

        } catch (Exception e) {
            throw new RuntimeException("Error starting in-process server",e);
        }
    }

    public boolean enableAuthentication() {
        return false;
    }

    public String authStoreLocation() {
        // creates an empty auth store, as this is a non-authenticating instance
        try {
            Path authStore = Files.createTempFile("neo4j", "credentials");
            authStore.toFile().deleteOnExit();
            return authStore.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initialise(ServerControls controls) throws Exception {

        Field field = InProcessServerControls.class.getDeclaredField("server");
        field.setAccessible(true);
        server = (AbstractNeoServer) field.get(controls);
        database = server.getDatabase().getGraph();

        // should be an http driver
        driver = Components.driver();

        configure(new Configuration());
    }

    public Driver driver() {
        return driver;
    }

    public synchronized void start() throws InterruptedException {
        server.start();
    }

    /**
     * Stops the underlying server bootstrapper and, in turn, the Neo4j server.
     */
    public synchronized void shutdown() {
        driver.close();
        controls.close();
        database.shutdown();
    }

    /**
     *
     * @param timeout
     * @return
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
        return server.baseUri().toString();
    }

    /**
     * Loads the specified CQL file from the classpath into the database.
     *
     * @param cqlFileName The name of the CQL file to load
     */
    public void loadClasspathCypherScriptFile(String cqlFileName) {
        new ExecutionEngine(this.database).execute(TestUtils.readCQLFile(cqlFileName).toString());
    }

    /**
     * Deletes all the nodes and relationships in the test database.
     */
    public void clearDatabase() {
        new ExecutionEngine(this.database).execute("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r, n");
    }

    /**
     * Retrieves the underlying {@link org.neo4j.graphdb.GraphDatabaseService} used in this test.
     *
     * @return The test {@link org.neo4j.graphdb.GraphDatabaseService}
     */
    public GraphDatabaseService getGraphDatabaseService() {
        return this.database;
    }


    @Override
    public void configure(Configuration config) {
        config.setConfig("server", url());
        driver.configure(config);
    }

    @Override
    public Object getConfig(String key) {
        return driver.getConfig(key);
    }

    @Override
    public Transaction newTransaction() {
        return driver.newTransaction();
    }

    @Override
    public void close() {
        shutdown();
    }

    @Override
    public Request requestHandler() {
        return driver.requestHandler();
    }

    @Override
    public void setTransactionManager(TransactionManager tx) {
        driver.setTransactionManager(tx);
    }
}
