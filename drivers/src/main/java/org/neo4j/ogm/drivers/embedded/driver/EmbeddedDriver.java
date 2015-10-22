package org.neo4j.ogm.drivers.embedded.driver;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.api.config.Configuration;
import org.neo4j.ogm.api.request.Request;
import org.neo4j.ogm.api.transaction.Transaction;
import org.neo4j.ogm.drivers.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.embedded.request.EmbeddedRequest;
import org.neo4j.ogm.drivers.embedded.transaction.EmbeddedTransaction;

/**
 * @author vince
 */
public class EmbeddedDriver extends AbstractConfigurableDriver {

    // a single instance of the driver's transport must be shared among all instances of the driver
    // so that we do not run into locking problems.

    private static GraphDatabaseService transport;


    /**
     * The default constructor will start a new embedded instance
     * using the default properties file.
     */
    public EmbeddedDriver() {
        configure(new Configuration("embedded.driver.properties"));
    }

    /**
     * This constructor allows the user to pass in an existing
     * Graph database service, e.g. if user code is running as an extension inside
     * an existing Neo4j server
     *
     * @param transport
     */
    public EmbeddedDriver(GraphDatabaseService transport) {
        this.transport = transport;
        configure(new Configuration("driver.properties.embedded"));
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
    public synchronized void configure(Configuration config) {

        super.configure(config);

        if (transport == null) {
            String storeDir = (String) config.getConfig("neo4j.store");
            transport = new GraphDatabaseFactory()
                    .newEmbeddedDatabaseBuilder( storeDir )
                    .newGraphDatabase();

            registerShutdownHook(transport);
        }

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

}
