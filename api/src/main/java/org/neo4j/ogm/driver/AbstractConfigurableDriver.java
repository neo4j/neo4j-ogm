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
package org.neo4j.ogm.driver;

import static java.util.stream.Collectors.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.ParameterConversion.DefaultParameterConversion;
import org.neo4j.ogm.spi.CypherModificationProvider;

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

    /**
     * Set of classes that are supported natively without implicit or explicit (native type support) conversion.
     * The set is not meant to be used outside this package, so it was put into the concrete driver class and not
     * on the {@link TypeSystem} interface.
     * </p>
     * The byte array ({@code byte[]} has been excluded here as OGM should use {@code org.neo4j.ogm.typeconversion.ByteArrayBase64Converter}
     * by default.
     * </p>
     * {@link List} and {@link Map} have both been excluded, as the {@link TypeSystemBasedParameterConversion} takes care of that.
     */
    static final Set<Class<?>> DEFAULT_SUPPORTED_TYPES =
        Stream.of(
            List.class,
            Map.class,
            Boolean.class, boolean.class,
            Long.class, long.class,
            Double.class, double.class,
            String.class
        ).collect(collectingAndThen(toSet(), Collections::unmodifiableSet));

    private final ThreadLocal<ServiceLoader<CypherModificationProvider>> cypherModificationProviderLoader =
        ThreadLocal.withInitial(() -> ServiceLoader.load(CypherModificationProvider.class));

    protected Configuration configuration;
    protected TypeSystem typeSystem = Driver.super.getTypeSystem();
    protected ParameterConversion parameterConversion = DefaultParameterConversion.INSTANCE;

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

    /**
     * Stores the configuration locally and loads the native type system for this driver if applicable. Be sure to call
     * this method in case you opt to overwrite it in an implementation.
     *
     * @param newConfiguration The new configuration
     */
    @Override
    public void configure(Configuration newConfiguration) {

        this.configuration = newConfiguration;
        initializeTypeSystem();
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public final Function<String, String> getCypherModification() {

        Function<String, String> loadedCypherModification = this.cypherModification;
        if (loadedCypherModification == null) {
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
                .forName(nativeTypesImplementation, true, Configuration.getDefaultClassLoader());

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
                throw new NativeTypesNotSupportedException(this.getClass().getName());
            } catch (ClassNotFoundException e) {
                throw new NativeTypesNotAvailableException(this.getClass().getName());
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

        if (this.configuration == null) {
            throw new IllegalStateException("Driver is not configured and cannot load Cypher modifications.");
        }

        return this.configuration.getCustomProperties();
    }

    private Function<String, String> loadCypherModifications() {

        Map<String, Object> configurationProperties = this.customPropertiesSupplier.get();
        ServiceLoader<CypherModificationProvider> currentProviderLoader = this.cypherModificationProviderLoader.get();
        currentProviderLoader.reload();

        return StreamSupport.stream(currentProviderLoader.spliterator(), false)
            .sorted(Comparator.comparing(CypherModificationProvider::getOrder))
            .map(provider -> provider.getCypherModification(configurationProperties))
            .reduce(Function.identity(), Function::andThen, Function::andThen);
    }
}
