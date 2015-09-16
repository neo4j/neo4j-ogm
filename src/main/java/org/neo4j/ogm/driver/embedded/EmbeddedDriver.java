package org.neo4j.ogm.driver.embedded;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.authentication.Neo4jCredentials;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public class EmbeddedDriver implements Driver<String> {

    private final Logger logger = LoggerFactory.getLogger(EmbeddedDriver.class);

    private GraphDatabaseService graphDb = null; //= new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
    private Neo4jCredentials credentials;
    private DriverConfig driverConfig;

    /**
     * Registers a shutdown hook for the Neo4j instance so that it
     * shuts down nicely when the VM exits (even if you "Ctrl-C" the
     * running application).
     *
     * @param graphDb the embedded instance to shutdown
     */
    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }

    @Override
    public synchronized void configure(DriverConfig config) {

        this.driverConfig = config;

        if (graphDb != null) {
            logger.warn("Instance is being re-configured");
            graphDb.shutdown();
        }

        String storeDir = config.getConfig("neo4j.store");

        // TODO: String ha = config.getConfig("ha");

        graphDb = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder( storeDir )
                .loadPropertiesFromFile("neo4j.properties" )
                .newGraphDatabase();
    }

    @Override
    public void rollback(Transaction tx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void commit(Transaction tx) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String newTransactionUrl(String host) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // should be configure. Then we can set up a config for each driver appropriately
    @Override
    public void authorize(Neo4jCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public void close() {
        if (graphDb != null) {
            graphDb.shutdown();
        }
    }

    @Override
    public Neo4jResponse<String> execute(String jsonStatements, Transaction tx) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
