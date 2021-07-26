/*
 * Copyright (c) 2002-2021 "Neo4j,"
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
import static org.neo4j.ogm.support.FileUtils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.embedded.request.EmbeddedRequest;
import org.neo4j.ogm.drivers.embedded.transaction.EmbeddedTransaction;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class EmbeddedDriver extends AbstractConfigurableDriver {

    private static final int TIMEOUT = 60_000;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedDriver.class);

    private DatabaseManagementService databaseManagementService;
    private GraphDatabaseService graphDatabaseService;
    /**
     * The database to use, defaults to {@literal null} (Use Neo4j default).
     */
    private String database = null;

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
    public synchronized void configure(Configuration newConfiguration) {

        super.configure(newConfiguration);
        this.database = this.configuration.getDatabase();

        try {
            String fileStoreUri = newConfiguration.getURI();

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

            DatabaseManagementServiceBuilder graphDatabaseBuilder = getGraphDatabaseFactory(newConfiguration, file);

            String neo4jConfLocation = newConfiguration.getNeo4jConfLocation();
            if (neo4jConfLocation != null) {
                URL neo4ConfUrl = newConfiguration.getResourceUrl(neo4jConfLocation);
                Path tmp = Files.createTempFile("neo4j-ogm-embedded", ".conf");
                try (BufferedReader in = new BufferedReader(new InputStreamReader(neo4ConfUrl.openStream()));
                    BufferedWriter out = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                    in.transferTo(out);
                    out.flush();
                }
                graphDatabaseBuilder = graphDatabaseBuilder.loadPropertiesFromFile(tmp.toFile().getAbsolutePath());
            }

            this.databaseManagementService = graphDatabaseBuilder.build();
            this.graphDatabaseService = this.databaseManagementService.database(getDatabase());
        } catch (Exception e) {
            throw new ConnectionException("Error connecting to embedded graph", e);
        }
    }

    private String getDatabase() {
        return Optional.ofNullable(this.database).filter(s -> !s.isBlank())
            .map(String::trim).orElse(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);
    }

    /**
     * Creates an instance of a {@code HighlyAvailableGraphDatabaseFactory} if requested by the config. Otherwise just
     * a standard one.
     *
     * @param configuration
     * @return
     * @throws Exception all the exceptions that might happen during dynamic construction of things.
     */
    private static DatabaseManagementServiceBuilder getGraphDatabaseFactory(Configuration configuration, File homeDir) throws Exception {

        DatabaseManagementServiceBuilder graphDatabaseFactory;
        if (!configuration.isEmbeddedHA()) {
            graphDatabaseFactory = new DatabaseManagementServiceBuilder(homeDir);
        } else {
            String classnameOfHaFactory = "com.neo4j.dbms.api.EnterpriseDatabaseManagementServiceBuilder";
            Class<? extends DatabaseManagementServiceBuilder> haFactoryClass = (Class<? extends DatabaseManagementServiceBuilder>) Class
                .forName(classnameOfHaFactory);
            graphDatabaseFactory = haFactoryClass.getDeclaredConstructor(File.class).newInstance(homeDir);
        }

        return graphDatabaseFactory;
    }

    @Override
    protected String getTypeSystemName() {
        return "org.neo4j.ogm.drivers.embedded.types.EmbeddedNativeTypes";
    }

    @Override
    public Function<TransactionManager, BiFunction<Transaction.Type, Iterable<String>, Transaction>> getTransactionFactorySupplier() {
        return transactionManager -> (type, bookmarks) -> {
            if (bookmarks != null && bookmarks.iterator().hasNext()) {
                logger.warn("Passing bookmarks {} to EmbeddedDriver. This is not currently supported.", bookmarks);
            }

            Transaction currentOGMTransaction = transactionManager.getCurrentTransaction();
            return new EmbeddedTransaction(transactionManager, nativeTransaction(currentOGMTransaction), type);
        };
    }

    @Override
    public synchronized void close() {

        if (databaseManagementService != null) {
            logger.info("Shutting down Embedded driver {} ", databaseManagementService);
            databaseManagementService.shutdown();
            databaseManagementService = null;
        }

        graphDatabaseService = null;
    }

    public <T> T unwrap(Class<T> clazz) {

        if (clazz == GraphDatabaseService.class) {
            return (T) graphDatabaseService;
        } else {
            return super.unwrap(clazz);
        }
    }

    @Override
    public Request request(Transaction transaction) {
        return new EmbeddedRequest(graphDatabaseService, transaction, parameterConversion, new EmbeddedEntityAdapter(typeSystem), getCypherModification());
    }

    private org.neo4j.graphdb.Transaction nativeTransaction(Transaction currentOGMTransaction) {

        org.neo4j.graphdb.Transaction nativeTransaction;

        if (currentOGMTransaction != null) {
            logger.debug("Using current transaction: {}", currentOGMTransaction);
            nativeTransaction = ((EmbeddedTransaction) currentOGMTransaction).getNativeTransaction();
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
