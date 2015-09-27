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

package org.neo4j.ogm.testutil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Scanner;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.harness.internal.InProcessServerControls;
import org.neo4j.helpers.Settings;
import org.neo4j.server.AbstractNeoServer;
import org.neo4j.shell.ShellSettings;

/**
 * @author Vince Bickers
 */
@SuppressWarnings("deprecation")
public class TestServer {

    private AbstractNeoServer server;
    private GraphDatabaseService database;

    public TestServer() {

        try {
            ServerControls controls = TestServerBuilders.newInProcessBuilder()
                    .withConfig("dbms.security.auth_enabled", "false")
                    .withConfig("org.neo4j.server.webserver.port", String.valueOf(TestUtils.getAvailablePort()))
                    .withConfig(ShellSettings.remote_shell_enabled.name(), Settings.FALSE)
                    .newServer();

            initialise(controls);

        } catch (Exception e) {
            throw new RuntimeException("Error starting in-process server",e);
        }

    }

    public TestServer(int port) {

        try {
            ServerControls controls = TestServerBuilders.newInProcessBuilder()
                    .withConfig("dbms.security.auth_enabled", "false")
                    .withConfig("org.neo4j.server.webserver.port", String.valueOf(port))
                    .withConfig(ShellSettings.remote_shell_enabled.name(), Settings.FALSE)
                    .newServer();

            initialise(controls);

        } catch (Exception e) {
            throw new RuntimeException("Error starting in-process server",e);
        }
    }

    private void initialise(ServerControls controls) throws Exception {

        Field field = InProcessServerControls.class.getDeclaredField("server");
        field.setAccessible(true);
        server = (AbstractNeoServer) field.get(controls);
        database = server.getDatabase().getGraph();
    }

    public synchronized void start() throws InterruptedException {
        server.start();
    }

    /**
     * Stops the underlying server bootstrapper and, in turn, the Neo4j server.
     */
    public synchronized void shutdown() {
        server.stop();
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
        StringBuilder cypher = new StringBuilder();
        try (Scanner scanner = new Scanner(Thread.currentThread().getContextClassLoader().getResourceAsStream(cqlFileName))) {
            scanner.useDelimiter(System.getProperty("line.separator"));
            while (scanner.hasNext()) {
                cypher.append(scanner.next()).append(' ');
            }
        }

        new ExecutionEngine(this.database).execute(cypher.toString());
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


}
