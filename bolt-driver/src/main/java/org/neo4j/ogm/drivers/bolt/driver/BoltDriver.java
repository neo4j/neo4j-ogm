/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.drivers.bolt.driver;

import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static org.neo4j.ogm.drivers.bolt.transaction.BoltTransaction.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.neo4j.driver.*;
import org.neo4j.driver.exceptions.ClientException;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.driver.internal.Scheme;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.Credentials;
import org.neo4j.ogm.config.UsernamePasswordCredentials;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.driver.ExceptionTranslator;
import org.neo4j.ogm.drivers.bolt.request.BoltRequest;
import org.neo4j.ogm.drivers.bolt.transaction.BoltTransaction;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class BoltDriver extends AbstractConfigurableDriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoltDriver.class);
    public static final String CONFIG_PARAMETER_BOLT_LOGGING = "Bolt_Logging";

    private final ExceptionTranslator exceptionTranslator = new BoltDriverExceptionTranslator();

    private volatile Driver boltDriver;
    private Credentials credentials;
    private Config driverConfig;
    /**
     * The database to use, defaults to {@literal null} (Use Neo4j default).
     */
    private String database = null;

    // required for service loader mechanism
    public BoltDriver() {
    }

    public BoltDriver(Driver boltDriver) {
        this(boltDriver, Collections::emptyMap);
    }

    /**
     * Create OGM BoltDriver with provided java neo4j driver
     *
     * @param boltDriver               instance of java neo4j driver
     * @param customPropertiesSupplier Hook to provide custom configuration properties, i.e. for Cypher modification providers
     */
    public BoltDriver(Driver boltDriver, Supplier<Map<String, Object>> customPropertiesSupplier) {

        super(customPropertiesSupplier);

        this.boltDriver = requireNonNull(boltDriver);
    }

    @Override
    public void configure(Configuration newConfiguration) {

        close();

        super.configure(newConfiguration);

        this.driverConfig = buildDriverConfig();
        this.credentials = this.configuration.getCredentials();
        this.database = this.configuration.getDatabase();

        if (this.configuration.getVerifyConnection()) {
            checkDriverInitialized();
        }
    }

    @Override
    protected String getTypeSystemName() {
        return "org.neo4j.ogm.drivers.bolt.types.BoltNativeTypes";
    }

    public Function<TransactionManager, BiFunction<Transaction.Type, Iterable<String>, Transaction>> getTransactionFactorySupplier() {
        return transactionManager -> (type, bookmarks) -> {
            checkDriverInitialized();

            //A bolt session can have at most one transaction running at a time
            Session session = newSession(type, bookmarks);
            return new BoltTransaction(transactionManager, session, type);
        };
    }

    private void checkDriverInitialized() {
        Driver driver = boltDriver;
        if (driver == null) {
            synchronized (this) {
                driver = boltDriver;
                if (driver == null) {
                    initializeDriver();
                }
            }
        }
    }

    static boolean isSimpleScheme(String scheme) {

        String lowerCaseScheme = scheme.toLowerCase(Locale.ENGLISH);
        try {
            Scheme.validateScheme(lowerCaseScheme);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(String.format("'%s' is not a supported scheme.", scheme));
        }

        return lowerCaseScheme.equals("bolt") || lowerCaseScheme.equals("neo4j");
    }

    private void initializeDriver() {

        final String serviceUnavailableMessage = "Could not create driver instance";
        Driver driver = null;
        try {
            if (credentials != null) {
                UsernamePasswordCredentials usernameAndPassword = (UsernamePasswordCredentials) this.credentials;
                AuthToken authToken = AuthTokens
                    .basic(usernameAndPassword.getUsername(), usernameAndPassword.getPassword());
                driver = createDriver(authToken);
            } else {
                LOGGER.debug("Bolt Driver credentials not supplied");
                driver = createDriver(AuthTokens.none());
            }
            driver.verifyConnectivity();
            boltDriver = driver;
            driver = null; // set null to skip close() in finally
        } catch (ServiceUnavailableException e) {
            throw new ConnectionException(serviceUnavailableMessage, e);
        } finally {
            if (driver != null) {
                driver.close();
            }
        }
    }

    private Driver createDriver(AuthToken authToken) {

        if (isRoutingConfig()) {
            return GraphDatabase.routingDriver(getMergedURIs(), authToken, driverConfig);
        } else {
            return GraphDatabase.driver(getSingleURI(), authToken, driverConfig);
        }
    }

    /**
     * It is a routing config if at least two URIs are defined.
     * Either URI and at least one value in URIS or two URIS.
     *
     * @return true, if more than one URI are defined in all URI related properties. Otherwise false.
     */
    private boolean isRoutingConfig() {
        String[] uris = configuration.getURIS();
        String uri = configuration.getURI();

        return uris != null && (uri == null && uris.length > 1 || uri != null && uris.length >= 1);
    }

    private List<URI> getMergedURIs() {
        List<URI> mergedUris = new ArrayList<>();
        String uri = configuration.getURI();
        String[] uris = configuration.getURIS();

        if (uri != null) {
            mergedUris.add(fixProtocolIfNecessary(URI.create(uri)));
        }
        if (uris != null) {
            for (String routingUri : uris) {
                mergedUris.add(fixProtocolIfNecessary(URI.create(routingUri)));
            }
        }

        return mergedUris;
    }

    private URI getSingleURI() {

        String singleUri;
        if (configuration.getURI() != null) {
            singleUri = configuration.getURI();
        } else {
            // if no URI was provided take the first argument from the URI list
            String[] uris = configuration.getURIS();
            if (uris == null || configuration.getURIS().length == 0) {
                throw new IllegalArgumentException(
                    "You must provide either an URI or at least one URI in the URIS parameter.");
            }
            singleUri = configuration.getURIS()[0];
        }

        return fixProtocolIfNecessary(URI.create(singleUri));
    }

    /**
     * Make the 4.0 driver somewhat backward compatible with older configurations
     * @param uri
     * @return
     */
    private static URI fixProtocolIfNecessary(URI uri) {
        if ("bolt+routing".equals(uri.getScheme().toLowerCase(Locale.ENGLISH))) {
            return URI.create(uri.toString().replaceAll("^bolt\\+routing", "neo4j"));
        }
        return uri;
    }

    @Override
    public synchronized void close() {
        if (boltDriver != null) {
            try {
                LOGGER.info("Shutting down Bolt driver {} ", boltDriver);
                boltDriver.close();
                boltDriver = null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public ExceptionTranslator getExceptionTranslator() {
        return this.exceptionTranslator;
    }

    @Override
    public Request request(Transaction transaction) {
        return new BoltRequest(transaction, this.parameterConversion, new BoltEntityAdapter(typeSystem), getCypherModification());
    }

    public <T> T unwrap(Class<T> clazz) {

        if (clazz == Driver.class) {
            return (T) boltDriver;
        } else {
            return super.unwrap(clazz);
        }
    }

    private Session newSession(Transaction.Type type, Iterable<String> bookmarks) {
        Session boltSession;
        try {
            AccessMode accessMode = type.equals(Transaction.Type.READ_ONLY) ? AccessMode.READ : AccessMode.WRITE;
            SessionConfig.Builder sessionConfigBuilder = SessionConfig.builder().withDefaultAccessMode(accessMode)
                .withBookmarks(bookmarksFromStrings(bookmarks));
            if (this.database != null) {
                sessionConfigBuilder = sessionConfigBuilder.withDatabase(database);
            }
            boltSession = boltDriver.session(sessionConfigBuilder.build());
        } catch (ClientException ce) {
            throw new ConnectionException(
                "Error connecting to graph database using Bolt: " + ce.code() + ", " + ce.getMessage(), ce);
        } catch (Exception e) {
            throw new ConnectionException("Error connecting to graph database using Bolt", e);
        }
        return boltSession;
    }

    private Optional<Logging> getBoltLogging() {

        Object possibleLogging = customPropertiesSupplier.get().get(CONFIG_PARAMETER_BOLT_LOGGING);
        if (possibleLogging != null && !(possibleLogging instanceof Logging)) {
            LOGGER.warn("Invalid object of type {} for {}, not changing log.", possibleLogging.getClass(),
                CONFIG_PARAMETER_BOLT_LOGGING);
            possibleLogging = null;
        }

        LOGGER.debug("Using {} for bolt logging.", possibleLogging == null ? "default" : possibleLogging.getClass());

        return Optional.ofNullable((Logging) possibleLogging);
    }

    private Config buildDriverConfig() {

        // Done outside the try/catch and explicity catch the illegalargument exception of singleURI
        // so that exception semantics are not changed since we introduced that feature.
        //
        // GraphDatabase.routingDriver asserts `neo4j` scheme for each URI, so our trust settings
        // have to be applied in this case.
        final boolean shouldApplyEncryptionAndTrustSettings;
        if (isRoutingConfig()) {
            shouldApplyEncryptionAndTrustSettings = true;
        } else { // Otherwise we check if it comes with the scheme or not.
            URI singleUri = null;
            try {
                singleUri = getSingleURI();
            } catch (IllegalArgumentException e) {
            }
            shouldApplyEncryptionAndTrustSettings = singleUri == null || isSimpleScheme(singleUri.getScheme());
        }

        try {
            Config.ConfigBuilder configBuilder = Config.builder();
            configBuilder.withMaxConnectionPoolSize(configuration.getConnectionPoolSize());

            if (shouldApplyEncryptionAndTrustSettings) {
                applyEncryptionAndTrustSettings(configBuilder);
            }

            if (configuration.getConnectionLivenessCheckTimeout() != null) {
                configBuilder.withConnectionLivenessCheckTimeout(configuration.getConnectionLivenessCheckTimeout(),
                    TimeUnit.MILLISECONDS);
            }

            getBoltLogging().ifPresent(configBuilder::withLogging);

            return configBuilder.build();
        } catch (Exception e) {
            throw new ConnectionException("Unable to build driver configuration", e);
        }
    }

    private void applyEncryptionAndTrustSettings(Config.ConfigBuilder configBuilder) {
        if (configuration.getEncryptionLevel() != null && "REQUIRED"
            .equals(configuration.getEncryptionLevel().toUpperCase(Locale.ENGLISH).trim())) {
            configBuilder.withEncryption();
        } else {
            configBuilder.withoutEncryption();
        }

        Config.TrustStrategy.Strategy trustStrategy;
        if (configuration.getTrustStrategy() != null) {

            String configuredTrustStrategy = configuration.getTrustStrategy().toUpperCase(Locale.ENGLISH).trim();
            if (Arrays.asList("TRUST_ON_FIRST_USE", "TRUST_SIGNED_CERTIFICATES")
                .contains(configuredTrustStrategy)) {
                String validNames = Arrays.stream(Config.TrustStrategy.Strategy.values()).map(
                    Config.TrustStrategy.Strategy::name).collect(joining(", "));
                throw new IllegalArgumentException(
                    "Truststrategy " + configuredTrustStrategy + " is no longer supported, please choose one of "
                        + validNames);
            }

            try {
                trustStrategy = Config.TrustStrategy.Strategy.valueOf(configuredTrustStrategy);
            } catch (IllegalArgumentException iae) {
                LOGGER.debug("Invalid configuration for the Bolt Driver Trust Strategy: {}",
                    configuration.getTrustStrategy());
                throw iae;
            }

            switch (trustStrategy) {
                case TRUST_ALL_CERTIFICATES:
                    configBuilder.withTrustStrategy(Config.TrustStrategy.trustAllCertificates());
                    break;
                case TRUST_SYSTEM_CA_SIGNED_CERTIFICATES:
                    configBuilder.withTrustStrategy(Config.TrustStrategy.trustSystemCertificates());
                    break;
                case TRUST_CUSTOM_CA_SIGNED_CERTIFICATES:
                    if (configuration.getTrustCertFile() == null) {
                        throw new IllegalArgumentException(
                            "Configured trust strategy requires a certificate file.");
                    }
                    configBuilder.withTrustStrategy(Config.TrustStrategy
                        .trustCustomCertificateSignedBy(new File(URI.create(configuration.getTrustCertFile()))));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown strategy." + trustStrategy);
            }
        }
    }

    @SuppressWarnings("deprecation")
    static List<Bookmark> bookmarksFromStrings(Iterable<String> bookmarks) {
        return StreamSupport.stream(bookmarks.spliterator(), false)
            .map(b -> Arrays.stream(b.split(BOOKMARK_SEPARATOR)).collect(
                collectingAndThen(Collectors.toSet(), Bookmark::from)))
            .collect(toList());
    }
}
