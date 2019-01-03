/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
import static org.neo4j.ogm.driver.ParameterConversionMode.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseBuilder;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.driver.ParameterConversion;
import org.neo4j.ogm.driver.ParameterConversionMode;
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

    public EmbeddedDriver(GraphDatabaseService graphDatabaseService) {
        this(graphDatabaseService, Collections::emptyMap);
    }

    /**
     * Create OGM EmbeddedDriver with provided embedded instance.
     *
     * @param graphDatabaseService     Preconfigured, embedded instance.
     * @param customPropertiesSupplier Hook to provide custom configuration properties, i.e. for Cypher modification providers
     */
    public EmbeddedDriver(GraphDatabaseService graphDatabaseService,
        Supplier<Map<String, Object>> customPropertiesSupplier) {

        super(customPropertiesSupplier);

        this.graphDatabaseService = requireNonNull(graphDatabaseService);
        boolean available = this.graphDatabaseService.isAvailable(TIMEOUT);
        if (!available) {
            throw new IllegalArgumentException("Provided GraphDatabaseService is not in usable state");
        }
    }

    /**
     * Recursively deletes a directory tree.
     *
     * @param directory
     * @throws IOException
     */
    private static void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
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

            GraphDatabaseBuilder graphDatabaseBuilder = getGraphDatabaseFactory(configuration)
                .newEmbeddedDatabaseBuilder(file);

            String neo4jConfLocation = config.getNeo4jConfLocation();
            if(neo4jConfLocation != null) {
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
            Class<GraphDatabaseFactory> haFactoryClass = (Class<GraphDatabaseFactory>) Class.forName(classnameOfHaFactory);
            graphDatabaseFactory = haFactoryClass.getDeclaredConstructor().newInstance();
        }

        return graphDatabaseFactory;
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
            getParameterConversion(), getCypherModification());
    }

    private ParameterConversion getParameterConversion() {

        ParameterConversionMode mode = (ParameterConversionMode) customPropertiesSupplier.get()
            .getOrDefault(ParameterConversionMode.CONFIG_PARAMETER_CONVERSION_MODE, CONVERT_ALL);
        switch (mode) {
            case CONVERT_ALL:
                return AbstractConfigurableDriver.CONVERT_ALL_PARAMETERS_CONVERSION;
            case CONVERT_NON_NATIVE_ONLY:
                return EmbeddedBasedParameterConversion.INSTANCE;
            default:
                throw new IllegalStateException("Unsupported conversion mode: " + mode.name() + " for Bolt-Transport.");
        }
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
