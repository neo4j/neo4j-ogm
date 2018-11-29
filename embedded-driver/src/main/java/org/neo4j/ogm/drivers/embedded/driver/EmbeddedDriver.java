/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

import static java.util.Objects.*;
import static org.neo4j.ogm.support.FileUtils.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.embedded.request.EmbeddedRequest;
import org.neo4j.ogm.drivers.embedded.transaction.EmbeddedTransaction;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.support.ResourceUtils;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 * @author Michael J. Simons
 */
public class EmbeddedDriver extends AbstractConfigurableDriver {

    private static final int TIMEOUT = 60_000;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedDriver.class);

    private GraphDatabaseService graphDatabaseService;

    // required for service loader mechanism
    public EmbeddedDriver() {
    }

    public EmbeddedDriver(GraphDatabaseService graphDatabaseService, Configuration configuration) {
        this(graphDatabaseService, configuration, Collections::emptyMap);
    }

    /**
     * Create OGM EmbeddedDriver with provided embedded instance.
     *
     * @param graphDatabaseService     Preconfigured, embedded instance.
     * @param customPropertiesSupplier Hook to provide custom configuration properties, i.e. for Cypher modification providers
     */
    public EmbeddedDriver(GraphDatabaseService graphDatabaseService,
        Configuration configuration,
        Supplier<Map<String, Object>> customPropertiesSupplier
    ) {

        super(customPropertiesSupplier);

        super.configure(configuration);

        this.graphDatabaseService = requireNonNull(graphDatabaseService);
        boolean available = this.graphDatabaseService.isAvailable(TIMEOUT);
        if (!available) {
            throw new IllegalArgumentException("Provided GraphDatabaseService is not in usable state");
        }
    }

    @Override
    public synchronized void configure(Configuration configuration) {

        super.configure(configuration);

        try {
            String fileStoreUri = configuration.getURI();

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

            GraphDatabaseBuilder graphDatabaseBuilder = getGraphDatabaseFactory(configuration)
                .newEmbeddedDatabaseBuilder(file);

            String neo4jConfLocation = configuration.getNeo4jConfLocation();
            if (neo4jConfLocation != null) {
                URL neo4ConfUrl = ResourceUtils.getResourceUrl(neo4jConfLocation);
                graphDatabaseBuilder = graphDatabaseBuilder.loadPropertiesFromURL(neo4ConfUrl);
            }

            this.graphDatabaseService = graphDatabaseBuilder.newGraphDatabase();
        } catch (Exception e) {
            throw new ConnectionException("Error connecting to embedded graph", e);
        }
    }

    /**
     * Creates an instance of a {@code HighlyAvailableGraphDatabaseFactory} if requested by the config. Otherwise just
     * a standard one.
     *
     * @param configuration
     * @return
     * @throws Exception all the exceptions that might happen during dynamic construction of things.
     */
    private static GraphDatabaseFactory getGraphDatabaseFactory(Configuration configuration) throws Exception {

        GraphDatabaseFactory graphDatabaseFactory;
        if (!configuration.isEmbeddedHA()) {
            graphDatabaseFactory = new GraphDatabaseFactory();
        } else {
            String classnameOfHaFactory = "org.neo4j.graphdb.factory.HighlyAvailableGraphDatabaseFactory";
            Class<GraphDatabaseFactory> haFactoryClass = (Class<GraphDatabaseFactory>) Class
                .forName(classnameOfHaFactory);
            graphDatabaseFactory = haFactoryClass.getDeclaredConstructor().newInstance();
        }

        return graphDatabaseFactory;
    }

    @Override
    protected String getTypeSystemName() {
        return "org.neo4j.ogm.drivers.embedded.types.EmbeddedNativeTypes";
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
        return new EmbeddedRequest(graphDatabaseService, transactionManager,
            parameterConversion, new EmbeddedEntityAdapter(typeSystem), getCypherModification());
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

            Path path = Files.createTempDirectory("neo4jTmpEmbedded.db");
            Path databasePath = Paths.get(path.toFile().getAbsolutePath() + "/database");
            Files.createDirectories(databasePath);
            final File f = databasePath.toFile();
            URI uri = f.toURI();
            final String databaseUriValue = uri.toString();
            logger.warn("Creating temporary file store: " + databaseUriValue);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                close();
                try {
                    logger.warn("Deleting temporary file store: " + databaseUriValue);
                    deleteDirectory(path);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete temporary files in " + databaseUriValue, e);
                }
            }));

            return databaseUriValue;
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
