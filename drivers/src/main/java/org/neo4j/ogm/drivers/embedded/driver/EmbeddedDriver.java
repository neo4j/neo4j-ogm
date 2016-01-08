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

    private GraphDatabaseService graphDatabaseService;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedDriver.class);

    // required for service loader mechanism
    public EmbeddedDriver() {
        logger.debug("*** starting new embedded driver instance via service loader, (not yet configured) " + this);
    }

    /**
     * Configure a new embedded driver according to the supplied driver configuration
     * @param driverConfiguration the {@link DriverConfiguration} to use
     */
    public EmbeddedDriver(DriverConfiguration driverConfiguration) {
        logger.debug("*** starting new embedded driver via explicit config " + this);
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
        this.graphDatabaseService = graphDatabaseService;
        registerShutdownHook();
    }

    /**
     * Registers a shutdown hook for the Neo4j instance so that it
     * shuts down nicely when the VM exits (even if you "Ctrl-C" the
     * running application).
     *
     */
    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }

    @Override
    public synchronized void configure(DriverConfiguration config) {

        super.configure(config);

        close();  // force any existing graph database to shutdown

        try {
            String fileStoreUri = config.getURI();

            // if no URI is set, create a temporary folder for the graph db
            // that will persist only for the duration of the JVM
            // This is effectively what the ImpermanentDatabase does.
            if (fileStoreUri == null) {
                fileStoreUri = createTemporaryEphemeralFileStore();
                config.setURI(fileStoreUri);
            }

            File file = new File(new URI(fileStoreUri));

            graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase(file.getAbsolutePath());

            registerShutdownHook();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Transaction newTransaction() {   // return a new, or join an existing transaction
        return new EmbeddedTransaction(transactionManager, nativeTransaction());
    }

    @Override
    public void close() {
        if (graphDatabaseService != null) {
            logger.debug(" *** Now shutting down embedded database instance: " + this);
            graphDatabaseService.shutdown();
            //
            graphDatabaseService = null;
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
