/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.neo4j.ogm.drivers.embedded.driver;

import static java.util.Objects.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

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

    private final Logger logger = LoggerFactory.getLogger(EmbeddedDriver.class);
    private static final int TIMEOUT = 60_000;

    private GraphDatabaseService graphDatabaseService;

    // required for service loader mechanism
    public EmbeddedDriver() {
    }

    public EmbeddedDriver(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = requireNonNull(graphDatabaseService);
        boolean available = this.graphDatabaseService.isAvailable(TIMEOUT);
        if (!available) {
            throw new IllegalArgumentException("Provided GraphDatabaseService is not in usable state");
        }
    }

    @Override
    public synchronized void configure(Configuration config) {

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
                setHAGraphDatabase(file,
                    Thread.currentThread().getContextClassLoader().getResource(haPropertiesFileName));
            } else {
                setGraphDatabase(file);
            }
        } catch (Exception e) {
            throw new ConnectionException("Error connecting to embedded graph", e);
        }
    }

    private void setHAGraphDatabase(File file, URL propertiesFileURL) {
        graphDatabaseService = new HighlyAvailableGraphDatabaseFactory().newEmbeddedDatabaseBuilder(file)
            .loadPropertiesFromURL(propertiesFileURL).newGraphDatabase();
    }

    private void setGraphDatabase(File file) {
        graphDatabaseService = new GraphDatabaseFactory().newEmbeddedDatabase(file);
    }

    @Override
    public Transaction newTransaction(Transaction.Type type, Iterable<String> bookmarks) {
        if (bookmarks != null && bookmarks.iterator().hasNext()) {
            logger.warn("Passing bookmarks {} to EmbeddedDriver. This is not currently supported.", bookmarks);
        }
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
                    throw new RuntimeException("Failed to delete temporary files in " + fileStoreUri, e);
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
                Path graphDir = Files.createDirectories(file.toPath());
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
