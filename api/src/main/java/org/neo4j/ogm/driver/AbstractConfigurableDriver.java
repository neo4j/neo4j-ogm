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

package org.neo4j.ogm.driver;

import java.util.Comparator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.spi.CypherModificationProvider;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * The AbstractConfigurableDriver is used by all drivers to register themselves.
 * The register method takes a generic {@link Configuration} object, which is used to register the
 * driver appropriately. This object contains of one or more key-value
 * pairs. Every driver configuration must contain a mandatory key "URI", whose corresponding value is
 * a text representation of the driver uri, for example:
 * setConfig("URI", "http://username:password@hostname:port")
 * if credentials are not present the URI, they can be specified in one of
 * two ways, either as a plain text username/password key-values pair in the configuration e.g.
 * setConfig("username", "bilbo")
 * setConfig("password", "hobbit")
 * or, alternatively using the "credentials" key
 * setConfig("credentials", new UsernamePasswordCredentials("bilbo", "hobbit")
 *
 * @author Vince Bickers
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public abstract class AbstractConfigurableDriver implements Driver {

    private final ServiceLoader<CypherModificationProvider> cypherModificationProviderLoader =
        ServiceLoader.load(CypherModificationProvider.class);

    protected Configuration configuration;
    protected TransactionManager transactionManager;

    private final Supplier<Map<String, Object>> configurationPropertiesSupplier;
    private volatile Function<String, String> cypherModification;


    public AbstractConfigurableDriver() {
        this.configurationPropertiesSupplier = this::getConfigurationProperties;
    }

    /**
     * This is only provided for the embedded driver that can take a preconfigured, embedded database
     * without any way to add configuration properties.
     *
     * @param configurationPropertiesSupplier
     */
    protected AbstractConfigurableDriver(Supplier<Map<String, Object>> configurationPropertiesSupplier) {
        this.configurationPropertiesSupplier = configurationPropertiesSupplier;
    }

    @Override
    public void configure(Configuration config) {
        this.configuration = config;
    }

    @Override
    public void setTransactionManager(TransactionManager transactionManager) {
        assert (transactionManager != null);
        this.transactionManager = transactionManager;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public final Function<String, String> getCypherModification() {

        Function<String, String> loadedCypherModification = this.cypherModification;
        if(loadedCypherModification == null) {
            synchronized (this) {
                if(this.cypherModification == null) {
                    loadedCypherModification = this.cypherModification = loadCypherModifications();
                }
            }
        }
        return loadedCypherModification;
    }

    private Map<String, Object> getConfigurationProperties() {
        if(this.configuration == null) {
            throw new IllegalStateException("Driver is not configured and cannot load Cypher modifications.");
        }

        return this.configuration.getConfigProperties();
    }

    private Function<String, String> loadCypherModifications() {

        Map<String, Object> configurationProperties = this.configurationPropertiesSupplier.get();
        this.cypherModificationProviderLoader.reload();

        return StreamSupport.stream(this.cypherModificationProviderLoader.spliterator(), false)
            .sorted(Comparator.comparing(CypherModificationProvider::getOrder))
            .map(provider -> provider.getCypherModification(configurationProperties))
            .reduce(Function.identity(), Function::andThen, Function::andThen);
    }
}
