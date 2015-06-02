package org.neo4j.ogm.testutil;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.server.configuration.Configurator;
import org.neo4j.server.configuration.ServerConfigurator;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

/**
 * @author Vince Bickers
 */
public class TestServer {

    private static final int DEFAULT_TEST_SERVER_PORT = 7575;

    private final int port;
    private WrappingNeoServerBootstrapper bootstrapper;
    private GraphDatabaseService database;
    private volatile boolean stopped = true;

    /**
     * Constructs a new {@link Neo4jIntegrationTestRule} that starts a Neo4j server listening on any available local port.
     */
    public TestServer() {
        this(findAvailableLocalPort());
    }

    /**
     * Constructs a new {@link Neo4jIntegrationTestRule} that starts a Neo4j server listening on the specified port.
     *
     * @param neoServerPort The local TCP port on which the Neo4j database server should run
     */
    public TestServer(int neoServerPort) {
        this.port = neoServerPort;
        this.database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        this.bootstrapper = createServerWrapper();

        start();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                shutdown();
            }
        });
    }

    private static int findAvailableLocalPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            System.err.println("Unable to establish local port for Neo4j test server due to IOException: " + e.getMessage()
                    + "\nDefaulting instead to use: " + DEFAULT_TEST_SERVER_PORT);
            e.printStackTrace(System.err);

            return DEFAULT_TEST_SERVER_PORT;
        }
    }

    private WrappingNeoServerBootstrapper createServerWrapper() {
        ServerConfigurator configurator = new ServerConfigurator((GraphDatabaseAPI) this.database);
        configurator.configuration().addProperty(Configurator.WEBSERVER_PORT_PROPERTY_KEY, this.port);
        configurator.configuration().setProperty("dbms.security.auth_enabled",false);
        return new WrappingNeoServerBootstrapper((GraphDatabaseAPI) this.database, configurator);
    }

    /**
     * Starts the underlying server bootstrapper and, in turn the Neo4j server
     */
    public synchronized void start() {
        if (stopped) {
            stopped = false;
            this.bootstrapper.start();
        }
    }

    /**
     * Stops the underlying server bootstrapper and, in turn, the Neo4j server.
     */
    public synchronized void shutdown() {
        if (!stopped) {
            stopped = true;
            this.bootstrapper.stop();
        }
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
        return "http://localhost:" + this.port;
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
     * Retrieves the underlying {@link GraphDatabaseService} used in this test.
     *
     * @return The test {@link GraphDatabaseService}
     */
    public GraphDatabaseService getGraphDatabaseService() {
        return this.database;
    }

}
