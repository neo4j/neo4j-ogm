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
package org.neo4j.ogm.drivers.bolt.driver;

import static java.util.Objects.*;
import static org.neo4j.ogm.driver.ParameterConversionMode.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Logging;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.Credentials;
import org.neo4j.ogm.config.UsernamePasswordCredentials;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.driver.ParameterConversion;
import org.neo4j.ogm.driver.ParameterConversionMode;
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

    private volatile Driver boltDriver;

    private Credentials credentials;
    private Config driverConfig;
    private Configuration configuration;

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
    public void configure(Configuration config) {

        close();

        super.configure(config);

        this.configuration = config;
        driverConfig = buildDriverConfig(config);
        credentials = config.getCredentials();

        if (config.getVerifyConnection()) {
            checkDriverInitialized();
        }
    }

    @Override
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

    private void initializeDriver() {

        final String serviceUnavailableMessage = "Could not create driver instance";
        try {
            if (credentials != null) {
                UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) this.credentials;
                AuthToken authToken = AuthTokens.basic(credentials.getUsername(), credentials.getPassword());
                boltDriver = createDriver(authToken);
            } else {
                try {
                    boltDriver = createDriver(AuthTokens.none());
                } catch (ServiceUnavailableException e) {
                    throw new ConnectionException(serviceUnavailableMessage, e);
                }
                LOGGER.debug("Bolt Driver credentials not supplied");
            }
        } catch (ServiceUnavailableException e) {
            throw new ConnectionException(serviceUnavailableMessage, e);
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
            mergedUris.add(URI.create(uri));
        }
        if (uris != null) {
            for (String routingUri : uris) {
                mergedUris.add(URI.create(routingUri));
            }
        }

        return mergedUris;
    }

    private URI getSingleURI() {

        if (configuration.getURI() != null) {
            return URI.create(configuration.getURI());
        }

        // if no URI was provided take the first argument from the URI list
        String[] uris = configuration.getURIS();
        if (uris == null || configuration.getURIS().length == 0) {
            throw new IllegalArgumentException(
                "You must provide either an URI or at least one URI in the URIS parameter.");
        }

        return URI.create(configuration.getURIS()[0]);
    }

    @Override
    public synchronized void close() {
        if (boltDriver != null) {
            try {
                LOGGER.info("Shutting down Bolt driver {} ", boltDriver);
                boltDriver.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Request request(Transaction transaction) {
        return new BoltRequest(transaction, getParameterConversion(), getCypherModification());
    }

    private ParameterConversion getParameterConversion() {

        ParameterConversionMode mode = (ParameterConversionMode) customPropertiesSupplier.get()
            .getOrDefault(ParameterConversionMode.CONFIG_PARAMETER_CONVERSION_MODE, CONVERT_ALL);
        switch (mode) {
            case CONVERT_ALL:
                return AbstractConfigurableDriver.CONVERT_ALL_PARAMETERS_CONVERSION;
            case CONVERT_NON_NATIVE_ONLY:
                return JavaDriverBasedParameterConversion.INSTANCE;
            default:
                throw new IllegalStateException("Unsupported conversion mode: " + mode.name() + " for Bolt-Transport.");
        }
    }

    private Session newSession(Transaction.Type type, Iterable<String> bookmarks) {
        Session boltSession;
        try {
            AccessMode accessMode = type.equals(Transaction.Type.READ_ONLY) ? AccessMode.READ : AccessMode.WRITE;
            boltSession = boltDriver.session(accessMode, bookmarks);
        } catch (ClientException ce) {
            throw new ConnectionException(
                "Error connecting to graph database using Bolt: " + ce.code() + ", " + ce.getMessage(), ce);
        } catch (Exception e) {
            throw new ConnectionException("Error connecting to graph database using Bolt", e);
        }
        return boltSession;
    }

    private Optional<Logging> getBoltLogging() throws Exception {

        Object possibleLogging = customPropertiesSupplier.get().get(CONFIG_PARAMETER_BOLT_LOGGING);
        if (possibleLogging != null && !(possibleLogging instanceof Logging)) {
            LOGGER.warn("Invalid object of type {} for {}, not changing log.", possibleLogging.getClass(),
                CONFIG_PARAMETER_BOLT_LOGGING);
            possibleLogging = null;
        }

        LOGGER.debug("Using {} for bolt logging.", possibleLogging == null ? "default" : possibleLogging.getClass());

        return Optional.ofNullable((Logging) possibleLogging);
    }

    private Config buildDriverConfig(Configuration ogmConfig) {
        try {
            Config.ConfigBuilder configBuilder = Config.build();
            configBuilder.withMaxSessions(ogmConfig.getConnectionPoolSize());

            Config.EncryptionLevel encryptionLevel = Config.EncryptionLevel.REQUIRED;

            if (ogmConfig.getEncryptionLevel() != null) {
                try {
                    encryptionLevel = Config.EncryptionLevel
                        .valueOf(ogmConfig.getEncryptionLevel().toUpperCase());
                } catch (IllegalArgumentException iae) {
                    LOGGER.debug("Invalid configuration for the Bolt Driver Encryption Level: {}",
                        ogmConfig.getEncryptionLevel());
                    throw iae;
                }
            }

            if (encryptionLevel == Config.EncryptionLevel.REQUIRED) {
                configBuilder.withEncryption();
            } else {
                configBuilder.withoutEncryption();
            }

            Config.TrustStrategy.Strategy trustStrategy;
            if (ogmConfig.getTrustStrategy() != null) {
                try {
                    trustStrategy = Config.TrustStrategy.Strategy.valueOf(ogmConfig.getTrustStrategy());
                } catch (IllegalArgumentException iae) {
                    LOGGER.debug("Invalid configuration for the Bolt Driver Trust Strategy: {}",
                        ogmConfig.getTrustStrategy());
                    throw iae;
                }

                if (ogmConfig.getTrustCertFile() == null) {
                    throw new IllegalArgumentException("Missing configuration value for trust.certificate.file");
                }

                File knownHostsFile = new File(new URI(ogmConfig.getTrustCertFile()));
                if (trustStrategy == Config.TrustStrategy.Strategy.TRUST_ON_FIRST_USE) {
                    configBuilder.withTrustStrategy(
                        Config.TrustStrategy.trustOnFirstUse(knownHostsFile));
                }
                if (trustStrategy == Config.TrustStrategy.Strategy.TRUST_SIGNED_CERTIFICATES) {
                    configBuilder.withTrustStrategy(
                        Config.TrustStrategy.trustSignedBy(knownHostsFile));
                }
            }
            if (ogmConfig.getConnectionLivenessCheckTimeout() != null) {
                configBuilder.withConnectionLivenessCheckTimeout(ogmConfig.getConnectionLivenessCheckTimeout(),
                    TimeUnit.MILLISECONDS);
            }

            getBoltLogging().ifPresent(configBuilder::withLogging);

            return configBuilder.toConfig();
        } catch (Exception e) {
            throw new ConnectionException("Unable to build driver configuration", e);
        }
    }
}
