/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.drivers.embedded.driver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.embedded.request.EmbeddedRequest;
import org.neo4j.ogm.drivers.embedded.transaction.EmbeddedTransaction;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public class EmbeddedDriver extends AbstractConfigurableDriver {

    private GraphDatabaseService graphDatabaseService;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedDriver.class);

    // required for service loader mechanism
    public EmbeddedDriver() {
    }

    /**
     * Configure a new embedded driver according to the supplied driver configuration
     *
     * @param configuration the {@link Configuration} to use
     */
    public EmbeddedDriver(Configuration configuration) {
        configure(configuration);
    }

    /**
     * This constructor allows the user to pass in an existing
     * Graph database service, e.g. if user code is running as an extension inside
     * an existing Neo4j server
     *
     * @param graphDatabaseService the embedded database instance
     */
    public EmbeddedDriver(GraphDatabaseService graphDatabaseService) {
        close();
        this.graphDatabaseService = graphDatabaseService;
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    @Override
    public synchronized void configure(Configuration config) {

        close();  // force any existing graph database to shutdown

        super.configure(config);

        try {
            String fileStoreUri = config.getURI();

            // if no URI is set, create a temporary folder for the graph db
            // that will persist only for the duration of the JVM
            // This is effectively what the ImpermanentDatabase does.
            if (fileStoreUri == null) {
                fileStoreUri = createTemporaryFileStore();
            } else {
                createPermanentFileStore(fileStoreUri);
            }

            File file = new File(new URI(fileStoreUri));
            if (!file.exists()) {
                throw new RuntimeException("Could not create/open filestore: " + fileStoreUri);
            }

            // do we want to start a HA instance or a community instance?
            String haPropertiesFileName = config.getNeo4jHaPropertiesFile();
            if (haPropertiesFileName != null) {
                setHAGraphDatabase(file, Thread.currentThread().getContextClassLoader().getResource(haPropertiesFileName));
            } else {
                setGraphDatabase(file);
            }
        } catch (Exception e) {
            throw new ConnectionException("Error connecting to embedded graph", e);
        }
    }

    private void setHAGraphDatabase(File file, URL propertiesFileURL) {
        graphDatabaseService = new HighlyAvailableGraphDatabaseFactory().newEmbeddedDatabaseBuilder(file).loadPropertiesFromURL(propertiesFileURL).newGraphDatabase();
    }

    private void setGraphDatabase(File file) {
        graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase(file);
    }

    @Override
    public Transaction newTransaction(Transaction.Type type, String bookmark) {
        return new EmbeddedTransaction(transactionManager, nativeTransaction(), type);
    }

    @Override
    public synchronized void close() {

        if (graphDatabaseService != null) {
            logger.info("Shutting down Embedded driver {} ", graphDatabaseService);
            graphDatabaseService.shutdown();
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
            nativeTransaction = ((EmbeddedTransaction) tx).getNativeTransaction();
        } else {
            logger.debug("No current transaction, starting a new one");
            nativeTransaction = graphDatabaseService.beginTx();
        }
        logger.debug("Native transaction: {}", nativeTransaction);
        return nativeTransaction;
    }

    private String createTemporaryFileStore() {

        try {

            Path path = Files.createTempDirectory("neo4j.db");
            final File f = path.toFile();
            URI uri = f.toURI();
            final String fileStoreUri = uri.toString();
            logger.warn("Creating temporary file store: " + fileStoreUri);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                close();
                try {
                    logger.warn("Deleting temporary file store: " + fileStoreUri);
                    FileUtils.deleteDirectory(f);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete temporary files in " + fileStoreUri);
                }
            }));

            return fileStoreUri;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createPermanentFileStore(String strPath) {

        try {
            URI uri = new URI(strPath);
            File file = new File(uri);
            if (!file.exists()) {
                Path graphDir = Files.createDirectories(Paths.get(uri.getRawPath()));
                logger.warn("Creating new permanent file store: " + graphDir.toString());
            }
            Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        } catch (FileAlreadyExistsException e) {
            logger.warn("Using existing permanent file store: " + strPath);
        } catch (IOException | URISyntaxException ioe) {
            throw new RuntimeException(ioe);
        }
    }
}
