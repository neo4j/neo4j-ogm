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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.neo4j.driver.v1.AccessMode;
import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class BoltDriver extends AbstractConfigurableDriver {

    private final Logger LOGGER = LoggerFactory.getLogger(BoltDriver.class);

    private final ExceptionTranslator exceptionTranslator = new BoltDriverExceptionTranslator();

    private volatile Driver boltDriver;
    private Credentials credentials;
    private Config driverConfig;

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

        if (this.configuration.getVerifyConnection()) {
            checkDriverInitialized();
        }
    }

    @Override
    protected String getTypeSystemName() {
        return "org.neo4j.ogm.drivers.bolt.types.BoltNativeTypes";
    }

    @Override
    public Transaction newTransaction(Transaction.Type type, Iterable<String> bookmarks) {
        checkDriverInitialized();
        Session session = newSession(type,
            bookmarks); //A bolt session can have at most one transaction running at a time
        return new BoltTransaction(transactionManager, nativeTransaction(session), session, type);
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
                UsernamePasswordCredentials usernameAndPassword = (UsernamePasswordCredentials) this.credentials;
                AuthToken authToken = AuthTokens.basic(usernameAndPassword.getUsername(), usernameAndPassword.getPassword());
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
    public Request request() {
        return new BoltRequest(transactionManager, this.parameterConversion, new BoltEntityAdapter(typeSystem), getCypherModification());
    }

    @Override
    public ExceptionTranslator getExceptionTranslator() {
        return this.exceptionTranslator;
    }

    public <T> T unwrap(Class<T> clazz) {

        if (clazz == Driver.class) {
            checkDriverInitialized();
            return (T) boltDriver;
        } else {
            return super.unwrap(clazz);
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

    private org.neo4j.driver.v1.Transaction nativeTransaction(Session session) {

        org.neo4j.driver.v1.Transaction nativeTransaction;

        Transaction tx = transactionManager.getCurrentTransaction();
        if (tx != null) {
            LOGGER.debug("Using current transaction: {}", tx);
            nativeTransaction = ((BoltTransaction) tx).nativeBoltTransaction();
        } else {
            LOGGER.debug("No current transaction, starting a new one");
            nativeTransaction = session.beginTransaction();
        }
        LOGGER.debug("Native transaction: {}", nativeTransaction);
        return nativeTransaction;
    }

    private BoltConfig getBoltConfiguration() {
        BoltConfig boltConfig = new BoltConfig();

        if (configuration.getEncryptionLevel() != null) {
            try {
                boltConfig.encryptionLevel = Config.EncryptionLevel
                    .valueOf(configuration.getEncryptionLevel().toUpperCase());
            } catch (IllegalArgumentException iae) {
                LOGGER.debug("Invalid configuration for the Bolt Driver Encryption Level: {}",
                    configuration.getEncryptionLevel());
                throw iae;
            }
        }

        boltConfig.sessionPoolSize = configuration.getConnectionPoolSize();

        if (configuration.getTrustStrategy() != null) {
            try {
                boltConfig.trustStrategy = Config.TrustStrategy.Strategy.valueOf(configuration.getTrustStrategy());
            } catch (IllegalArgumentException iae) {
                LOGGER.debug("Invalid configuration for the Bolt Driver Trust Strategy: {}",
                    configuration.getTrustStrategy());
                throw iae;
            }
        }

        if (configuration.getTrustCertFile() != null) {
            boltConfig.trustCertFile = configuration.getTrustCertFile();
        }

        if (configuration.getConnectionLivenessCheckTimeout() != null) {
            boltConfig.connectionLivenessCheckTimeout = configuration.getConnectionLivenessCheckTimeout();
        }

        return boltConfig;
    }

    private Config buildDriverConfig() {
        try {
            BoltConfig boltConfig = getBoltConfiguration();
            Config.ConfigBuilder configBuilder = Config.build();
            configBuilder.withMaxSessions(boltConfig.sessionPoolSize);
            if (boltConfig.encryptionLevel.equals(Config.EncryptionLevel.REQUIRED)) {
                configBuilder.withEncryption();
            } else {
                configBuilder.withoutEncryption();
            }
            if (boltConfig.trustStrategy != null) {
                if (boltConfig.trustCertFile == null) {
                    throw new IllegalArgumentException("Missing configuration value for trust.certificate.file");
                }
                if (boltConfig.trustStrategy.equals(Config.TrustStrategy.Strategy.TRUST_ON_FIRST_USE)) {
                    configBuilder.withTrustStrategy(
                        Config.TrustStrategy.trustOnFirstUse(new File(new URI(boltConfig.trustCertFile))));
                }
                if (boltConfig.trustStrategy.equals(Config.TrustStrategy.Strategy.TRUST_SIGNED_CERTIFICATES)) {
                    configBuilder.withTrustStrategy(
                        Config.TrustStrategy.trustSignedBy(new File(new URI(boltConfig.trustCertFile))));
                }
            }
            if (boltConfig.connectionLivenessCheckTimeout != null) {
                configBuilder.withConnectionLivenessCheckTimeout(boltConfig.connectionLivenessCheckTimeout,
                    TimeUnit.MILLISECONDS);
            }

            return configBuilder.toConfig();
        } catch (Exception e) {
            throw new ConnectionException("Unable to build driver configuration", e);
        }
    }

    class BoltConfig {

        public static final int DEFAULT_SESSION_POOL_SIZE = 50;
        Config.EncryptionLevel encryptionLevel = Config.EncryptionLevel.REQUIRED;
        int sessionPoolSize = DEFAULT_SESSION_POOL_SIZE;
        Config.TrustStrategy.Strategy trustStrategy;
        String trustCertFile;
        Integer connectionLivenessCheckTimeout;
    }

}
