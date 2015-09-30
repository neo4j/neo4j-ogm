package org.neo4j.ogm.driver.embedded.driver;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.session.Driver;
import org.neo4j.ogm.session.DriverConfig;
import org.neo4j.ogm.driver.embedded.request.EmbeddedRequest;
import org.neo4j.ogm.driver.embedded.transaction.EmbeddedTransaction;
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

    private GraphDatabaseService transport;
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

        if (transport != null) {
            logger.warn("Instance is being re-configured");
            transport.shutdown();
        }

        String storeDir = (String) config.getConfig("neo4j.store");

        // TODO: String ha = config.getConfig("ha");

        transport = new GraphDatabaseFactory()
                .newEmbeddedDatabaseBuilder( storeDir )
                .newGraphDatabase();

        registerShutdownHook(transport);

        config.setConfig("transport", transport);
    }

    @Override
    public Transaction newTransaction() {
        return new EmbeddedTransaction(transactionManager, transport);
    }

    @Override
    public void close() {
        if (transport != null) {
            transport.shutdown();
        }
    }

    @Override
    public Request requestHandler() {
        return new EmbeddedRequest(transport);
    }

    @Override
    public Object getConfig(String key) {
        return driverConfig.getConfig(key);
    }

    @Override
    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
