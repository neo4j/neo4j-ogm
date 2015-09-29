package org.neo4j.ogm.driver.embedded.driver;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.driver.embedded.request.EmbeddedRequest;
import org.neo4j.ogm.driver.embedded.transaction.EmbeddedTransaction;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.request.Request;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public class EmbeddedDriver implements Driver {

    private final Logger logger = LoggerFactory.getLogger(EmbeddedDriver.class);

    private GraphDatabaseService graphDb = null;
    private DriverConfig driverConfig;
    private TransactionManager transactionManager;

    public EmbeddedDriver() {
        configure(new DriverConfig("driver.properties.embedded"));
    }

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

        String storeDir = (String) config.getConfig("neo4j.store");

        // TODO: String ha = config.getConfig("ha");

        graphDb = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder( storeDir )
                .newGraphDatabase();

        registerShutdownHook(graphDb);

        config.setConfig("graphDb", graphDb);
    }

    @Override
    public Transaction newTransaction(MappingContext context, TransactionManager tx, boolean autoCommit) {
        return new EmbeddedTransaction(context, tx, autoCommit, graphDb);
    }

    @Override
    public void close() {
        if (graphDb != null) {
            graphDb.shutdown();
        }
    }

    @Override
    public Request requestHandler() {
        return new EmbeddedRequest(graphDb);
    }

    @Override
    public Object getConfig(String key) {
        return driverConfig.getConfig(key);
    }

    @Override
    public TransactionManager transactionManager() {
        return transactionManager;
    }

    @Override
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
