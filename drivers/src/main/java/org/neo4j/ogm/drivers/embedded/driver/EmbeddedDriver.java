package org.neo4j.ogm.drivers.embedded.driver;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.drivers.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.embedded.request.EmbeddedRequest;
import org.neo4j.ogm.drivers.embedded.transaction.EmbeddedTransaction;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author vince
 */
public class EmbeddedDriver extends AbstractConfigurableDriver {

    // a single instance of the driver's graphDatabaseService must be shared among all instances of the driver
    // so that we do not run into locking problems.

    private static GraphDatabaseService graphDatabaseService;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedDriver.class);

    // required for service loader mechanism
    public EmbeddedDriver() {
    }

    /**
     * Configure a new embedded driver according to the supplied driver configuration
     * @param driverConfiguration the {@link DriverConfiguration} to use
     */
    public EmbeddedDriver(DriverConfiguration driverConfiguration) {
        configure(driverConfiguration);
    }

    /**
     * This constructor allows the user to pass in an existing
     * Graph database service, e.g. if user code is running as an extension inside
     * an existing Neo4j server
     *
     * @param graphDatabaseService the embedded database instance
     */
    public EmbeddedDriver(GraphDatabaseService graphDatabaseService) {
        EmbeddedDriver.graphDatabaseService = graphDatabaseService;
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
    public synchronized void configure(DriverConfiguration config) {

        super.configure(config);

        if (graphDatabaseService == null) {
            try {
                String fileStoreUri = config.getURI();
                if (fileStoreUri == null) {
                    fileStoreUri = createTemporaryEphemeralFileStore();
                    config.setURI(fileStoreUri);
                }
                File file = new File(new URI(fileStoreUri));
                graphDatabaseService = new GraphDatabaseFactory()
                        .newEmbeddedDatabaseBuilder(file.getAbsolutePath())
                        .newGraphDatabase();
                registerShutdownHook(graphDatabaseService);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Transaction newTransaction() {   // return a new, or join an existing transaction
        return new EmbeddedTransaction(transactionManager, nativeTransaction());
    }

    @Override
    public void close() {
        if (graphDatabaseService != null) {
            graphDatabaseService.shutdown();
        }
    }

    public GraphDatabaseService getGraphDatabaseService() {
        return graphDatabaseService;
    }

    @Override
    public Request request() {
        return new EmbeddedRequest(graphDatabaseService, transactionManager);
    }

    private org.neo4j.graphdb.Transaction nativeTransaction() {

        org.neo4j.graphdb.Transaction nativeTransaction;

        Transaction tx = transactionManager.getCurrentTransaction();
        if (tx != null) {
            logger.debug("Using current transaction: {}", tx);
            nativeTransaction =((EmbeddedTransaction) tx).getNativeTransaction();
        } else {
            logger.debug("No current transaction, starting a new one");
            nativeTransaction = graphDatabaseService.beginTx();
        }
        logger.debug("Native transaction: {}", nativeTransaction);
        return nativeTransaction;
    }

    private String createTemporaryEphemeralFileStore() {

        try {
            System.out.format("java tmpdir root: %s\n", System.getProperty("java.io.tmpdir"));

            Path path = Files.createTempDirectory("neo4j.db");
            System.out.format("Check temporary directory %s\n", path.toString());

            File f = path.toFile();
            System.out.format("Checking directory actually exists as a file %s\n", f.exists());

            f.deleteOnExit();
            URI uri = f.toURI();

            System.out.format("Checking URI object is not null: %s\n", uri != null);
            System.out.format("Checking URI as String %s\n", uri.toString());

            String fileStoreUri = uri.toString();

            return fileStoreUri;
        } catch (Exception e) {
            System.out.println("Caught an exception:");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
