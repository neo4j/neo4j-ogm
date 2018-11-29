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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.ParameterConversion.DefaultParameterConversion;
import org.neo4j.ogm.spi.CypherModificationProvider;
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected TypeSystem typeSystem = Driver.super.getTypeSystem();
    protected ParameterConversion parameterConversion = DefaultParameterConversion.INSTANCE;
    protected TransactionManager transactionManager;

    /**
     * Used for configuring the cypher modification providers. Defaults to {@link #getConfiguration()} and reads the
     * custom properties from the common {@link Configuration}.
     */
    protected final Supplier<Map<String, Object>> customPropertiesSupplier;
    /**
     * Final Cypher modififcation loaded from all present providers.
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

    /**
     * Stores the configuration locally and loads the native type system for this driver if applicable. Be sure to call
     * this method in case you opt to overwrite it in an implementation.
     *
     * @param configuration The new configuration
     */
    @Override
    public void configure(Configuration configuration) {

        this.configuration = configuration;
        initializeTypeSystem();
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

    /**
     * Utility method to load the dedicated driver version of native types.
     *
     * @param nativeTypesImplementation the fully qualified name of the class implementing this drivers' natives types.
     * @return A fully loaded and initialized instance of the class qualified by <code>nativeTypesImplementation</code>
     * @throws ClassNotFoundException If the required implementation is not on the classpath. Initialization should terminate then.
     * @since 3.2
     */
    private static TypeSystem loadNativeTypes(String nativeTypesImplementation) throws ClassNotFoundException {

        try {
            Class<TypeSystem> nativeTypesClass = (Class<TypeSystem>) Class
                .forName(nativeTypesImplementation, true, AbstractConfigurableDriver.class.getClassLoader());

            Constructor<TypeSystem> ctor = nativeTypesClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException("Could not load native types implementation " + nativeTypesImplementation);
        }
    }

    /**
     * Initializes the configured type system.
     *
     * @throws IllegalStateException In case the driver supports native types but those types aren't on the class path
     * @since 3.2
     */
    private void initializeTypeSystem() {

        if (this.configuration == null || !this.configuration.getUseNativeTypes()) {
            this.typeSystem = TypeSystem.NoNativeTypes.INSTANCE;
            this.parameterConversion = DefaultParameterConversion.INSTANCE;
        } else {
            try {
                this.typeSystem = loadNativeTypes(getTypeSystemName());
                this.parameterConversion = new TypeSystemBasedParameterConversion(this.typeSystem);
            } catch (UnsupportedOperationException e) {
                throw new IllegalStateException("Neo4j-OGM driver " + this.getClass().getName() + " doesn't support native types.");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(
                    "Cannot use native types. Make sure you have the native module for your driver on the classpath.");
            }
        }
    }

    @Override
    public final TypeSystem getTypeSystem() {
        return this.typeSystem;
    }

    /**
     * @return The fully qualified name of the native typesystem to use.
     * @throws UnsupportedOperationException in case the concrete driver doesn't support a native typesystem.
     */
    abstract protected String getTypeSystemName();

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
