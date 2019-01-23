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

    public static final ParameterConversion CONVERT_ALL_PARAMETERS_CONVERSION = ObjectMapperBasedParameterConversion.INSTANCE;

    private final ServiceLoader<CypherModificationProvider> cypherModificationProviderLoader =
        ServiceLoader.load(CypherModificationProvider.class);

    protected Configuration configuration;
    protected TransactionManager transactionManager;

    /**
     * Used for configuring the cypher modification providers. Defaults to {@link #getConfiguration()} and reads the
     * custom properties from the common {@link Configuration}.
     */
    protected final Supplier<Map<String, Object>> customPropertiesSupplier;
    /**
     * Final Cypher modification loaded from all present providers.
     */
    private volatile Function<String, String> cypherModification;

    public AbstractConfigurableDriver() {
        this.customPropertiesSupplier = this::getConfigurationProperties;
    }

    /**
     * This is only provided for the embedded driver that can take a preconfigured, embedded database
     * without any way to add configuration properties.
     *
     * @param customPropertiesSupplier
     */
    protected AbstractConfigurableDriver(Supplier<Map<String, Object>> customPropertiesSupplier) {
        this.customPropertiesSupplier = customPropertiesSupplier;
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
                loadedCypherModification = this.cypherModification;
                if (loadedCypherModification == null) {
                    this.cypherModification = loadCypherModifications();
                    loadedCypherModification = this.cypherModification;
                }
            }
        }
        return loadedCypherModification;
    }

    private Map<String, Object> getConfigurationProperties() {

        if(this.configuration == null) {
            throw new IllegalStateException("Driver is not configured and cannot load Cypher modifications.");
        }

        return this.configuration.getCustomProperties();
    }

    private Function<String, String> loadCypherModifications() {

        Map<String, Object> configurationProperties = this.customPropertiesSupplier.get();
        this.cypherModificationProviderLoader.reload();

        return StreamSupport.stream(this.cypherModificationProviderLoader.spliterator(), false)
            .sorted(Comparator.comparing(CypherModificationProvider::getOrder))
            .map(provider -> provider.getCypherModification(configurationProperties))
            .reduce(Function.identity(), Function::andThen, Function::andThen);
    }
}
