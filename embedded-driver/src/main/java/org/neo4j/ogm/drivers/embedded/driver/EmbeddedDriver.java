/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.config.DriverConfiguration;
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
public class EmbeddedDriver extends AbstractConfigurableDriver
{

    private GraphDatabaseService graphDatabaseService;
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
            }

            File file = new File(new URI(fileStoreUri));

            graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase(file);

            registerShutdownHook();
        } catch (Exception e) {
            throw new ConnectionException("Error connecting to embedded graph", e);
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
            Path path = Files.createTempDirectory("neo4j.db");
            File f = path.toFile();
            f.deleteOnExit();
            URI uri = f.toURI();
            String fileStoreUri = uri.toString();
            return fileStoreUri;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
