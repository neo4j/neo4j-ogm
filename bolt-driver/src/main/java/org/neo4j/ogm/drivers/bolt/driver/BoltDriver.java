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

package org.neo4j.ogm.drivers.bolt.driver;

import static java.util.Objects.*;
import static org.neo4j.ogm.driver.ParameterConversionMode.*;

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
import org.neo4j.ogm.driver.ParameterConversion;
import org.neo4j.ogm.driver.ParameterConversionMode;
import org.neo4j.ogm.drivers.bolt.request.BoltRequest;
import org.neo4j.ogm.drivers.bolt.transaction.BoltTransaction;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.driver.TypeSystem;
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

    private volatile Driver boltDriver;
    // It's a bit annoying to have the defaults repeated here but with BoltDriver being configurable without
    // the configuration just by passing in the Java-Driver, there's no other way.
    private TypeSystem typeSystem = TypeSystem.NoNativeTypes.INSTANCE;

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
        this.driverConfig = buildDriverConfig(this.configuration);
        this.credentials = this.configuration.getCredentials();
        this.typeSystem = loadTypeSystem("org.neo4j.ogm.drivers.bolt.types.BoltNativeTypes");

        if (this.configuration.getVerifyConnection()) {
            checkDriverInitialized();
        }
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
                UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) this.credentials;
                AuthToken authToken = AuthTokens.basic(credentials.getUsername(), credentials.getPassword());
                boltDriver = createDriver(configuration, driverConfig, authToken);
            } else {
                try {
                    boltDriver = createDriver(configuration, driverConfig, AuthTokens.none());
                } catch (ServiceUnavailableException e) {
                    throw new ConnectionException(serviceUnavailableMessage, e);
                }
                LOGGER.debug("Bolt Driver credentials not supplied");
            }
        } catch (ServiceUnavailableException e) {
            throw new ConnectionException(serviceUnavailableMessage, e);
        }
    }

    private Driver createDriver(Configuration config, Config driverConfig, AuthToken authToken) {
        if (config.getURIS() == null) {
            return GraphDatabase.driver(config.getURI(), authToken, driverConfig);
        } else {
            List<URI> uris = new ArrayList<>();
            uris.add(URI.create(config.getURI()));
            for (String additionalURI : config.getURIS()) {
                uris.add(URI.create(additionalURI));
            }

            return GraphDatabase.routingDriver(uris, authToken, driverConfig);
        }
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
        return new BoltRequest(transactionManager, new BoltEntityAdapter(typeSystem), getCypherModification());
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

    private BoltConfig getBoltConfiguration(Configuration configuration) {
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

    private Config buildDriverConfig(Configuration driverConfig) {
        try {
            BoltConfig boltConfig = getBoltConfiguration(driverConfig);
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

    public TypeSystem getTypeSystem() {
        return this.typeSystem;
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
