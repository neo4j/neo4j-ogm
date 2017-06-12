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

package org.neo4j.ogm.drivers.bolt.driver;

import java.io.File;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.UsernamePasswordCredentials;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.bolt.request.BoltRequest;
import org.neo4j.ogm.drivers.bolt.transaction.BoltTransaction;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class BoltDriver extends AbstractConfigurableDriver {

    private final Logger LOGGER = LoggerFactory.getLogger(BoltDriver.class);

    private Driver boltDriver;

    // required for service loader mechanism
    public BoltDriver() {
    }

    /**
     * Create OGM BoltDriver with provided java neo4j driver
     *
     * @param boltDriver instance of java neo4j driver
     */
    public BoltDriver(Driver boltDriver) {
        this.boltDriver = requireNonNull(boltDriver);
    }

    @Override
    public void configure(Configuration config) {

        close();

        super.configure(config);

        Config driverConfig = buildDriverConfig(config);

        if (config.getCredentials() != null) {
            UsernamePasswordCredentials credentials = (UsernamePasswordCredentials) config.getCredentials();
            AuthToken authToken = AuthTokens.basic(credentials.getUsername(), credentials.getPassword());
            boltDriver = GraphDatabase.driver(config.getURI(), authToken, driverConfig);
        } else {
            boltDriver = GraphDatabase.driver(config.getURI(), driverConfig);
            LOGGER.debug("Bolt Driver credentials not supplied");
        }
    }

    @Override
    public Transaction newTransaction(Transaction.Type type, Iterable<String> bookmarks) {
        Session session = newSession(type, bookmarks); //A bolt session can have at most one transaction running at a time
        return new BoltTransaction(transactionManager, nativeTransaction(session), session, type);
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
        return new BoltRequest(transactionManager);
    }

    private Session newSession(Transaction.Type type, Iterable<String> bookmarks) {
        Session boltSession;
        try {
            AccessMode accessMode = type.equals(Transaction.Type.READ_ONLY) ? AccessMode.READ : AccessMode.WRITE;
            boltSession = boltDriver.session(accessMode, bookmarks);
        } catch (ClientException ce) {
            throw new ConnectionException("Error connecting to graph database using Bolt: " + ce.code() + ", " + ce.getMessage(), ce);
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
                boltConfig.encryptionLevel = Config.EncryptionLevel.valueOf(configuration.getEncryptionLevel().toUpperCase());
            } catch (IllegalArgumentException iae) {
                LOGGER.debug("Invalid configuration for the Bolt Driver Encryption Level: {}", configuration.getEncryptionLevel());
                throw iae;
            }
        }

        boltConfig.sessionPoolSize = configuration.getConnectionPoolSize();

        if (configuration.getTrustStrategy() != null) {
            try {
                boltConfig.trustStrategy = Config.TrustStrategy.Strategy.valueOf(configuration.getTrustStrategy());
            } catch (IllegalArgumentException iae) {
                LOGGER.debug("Invalid configuration for the Bolt Driver Trust Strategy: {}", configuration.getTrustStrategy());
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
                    configBuilder.withTrustStrategy(Config.TrustStrategy.trustOnFirstUse(new File(new URI(boltConfig.trustCertFile))));
                }
                if (boltConfig.trustStrategy.equals(Config.TrustStrategy.Strategy.TRUST_SIGNED_CERTIFICATES)) {
                    configBuilder.withTrustStrategy(Config.TrustStrategy.trustSignedBy(new File(new URI(boltConfig.trustCertFile))));
                }
            }
            if (boltConfig.connectionLivenessCheckTimeout != null) {
                configBuilder.withConnectionLivenessCheckTimeout(boltConfig.connectionLivenessCheckTimeout, TimeUnit.MILLISECONDS);
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
