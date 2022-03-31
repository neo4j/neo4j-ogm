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
package org.neo4j.ogm.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import org.neo4j.ogm.support.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic configuration class that can be set up programmatically
 * or via a properties file.
 *
 * @author Vince Bickers
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private static final int DEFAULT_SESSION_POOL_SIZE = 50;

    /**
     * Configuration to change the precedence from the current threads context
     */
    public enum ClassLoaderPrecedence {
        /**
         * Use the current threads context class loader.
         */
        CONTEXT_CLASS_LOADER,
        /**
         * Use the class loader into which OGM was loaded.
         */
        OGM_CLASS_LOADER
    }

    public static final ThreadLocal<ClassLoaderPrecedence> CLASS_LOADER_PRECEDENCE = ThreadLocal
        .withInitial(() -> ClassLoaderPrecedence.CONTEXT_CLASS_LOADER);

    /**
     * @return The classloader to be used by OGM.
     */
    public static ClassLoader getDefaultClassLoader() {

        ClassLoaderPrecedence precedence = Configuration.CLASS_LOADER_PRECEDENCE.get();
        boolean ctclFirst = precedence == null || precedence == ClassLoaderPrecedence.CONTEXT_CLASS_LOADER;
        ClassLoader cl = null;
        try {
            cl = ctclFirst ? Thread.currentThread().getContextClassLoader() : ClassUtils.class.getClassLoader();
        } catch (Throwable ex) {
        }
        if (cl == null) {
            cl = ctclFirst ? ClassUtils.class.getClassLoader() : Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                }
            }
        }
        return cl;
    }

    private String uri;
    private String[] uris;
    private int connectionPoolSize;
    private String encryptionLevel;
    private String trustStrategy;
    private String trustCertFile;
    private AutoIndexMode autoIndex;
    private String generatedIndexesOutputDir;
    private String generatedIndexesOutputFilename;
    /**
     * The url of a neo4j.conf (properties) file to configure the embedded driver.
     */
    private String neo4jConfLocation;
    private String driverName;
    private Credentials credentials;
    private Integer connectionLivenessCheckTimeout;
    private Boolean verifyConnection;
    private Boolean useNativeTypes;
    private Map<String, Object> customProperties;
    /**
     * This flag instructs OGM to use all static labels when querying domain objects. Until 3.1.16 only the label of the
     * concrete domain has been used to query domain objects in inheritance scenarios. When storing those objects again,
     * OGM writes all labels in any case.
     * <p>
     * Using all reachable, static labels in a class hierarchy for querying and thus enforcing strict queries is the default
     * in Neo4j-OGM 4.0. Use this flag to restore the old behaviour.
     */
    private Boolean useStrictQuerying;
    /**
     * Base packages to scan for annotated components. They will be merged into a unique list
     * of packages with the programmatically registered packages to scan.
     */
    private String[] basePackages;
    private String database;

    /**
     * Protected constructor of the Configuration class.
     * Use {@link Builder} to create an instance of Configuration class
     */
    Configuration(Builder builder) {
        this.uri = builder.uri;
        this.uris = builder.uris;
        this.connectionPoolSize = builder.connectionPoolSize != null ? builder.connectionPoolSize : DEFAULT_SESSION_POOL_SIZE;
        this.encryptionLevel = builder.encryptionLevel;
        this.trustStrategy = builder.trustStrategy;
        this.trustCertFile = builder.trustCertFile;
        this.connectionLivenessCheckTimeout = builder.connectionLivenessCheckTimeout;
        this.verifyConnection = builder.verifyConnection != null ? builder.verifyConnection : false;
        this.autoIndex = builder.autoIndex != null ? AutoIndexMode.fromString(builder.autoIndex) : AutoIndexMode.NONE;
        this.generatedIndexesOutputDir =
            builder.generatedIndexesOutputDir != null ? builder.generatedIndexesOutputDir : ".";
        this.generatedIndexesOutputFilename = builder.generatedIndexesOutputFilename != null ?
            builder.generatedIndexesOutputFilename :
            "generated_indexes.cql";
        this.neo4jConfLocation = builder.neo4jConfLocation;
        this.customProperties = builder.customProperties;
        this.useNativeTypes = builder.useNativeTypes;
        this.basePackages = builder.basePackages;
        this.useStrictQuerying = builder.useStrictQuerying;
        this.database = Optional.ofNullable(builder.database).map(String::trim).filter(s -> !s.isEmpty()).orElse(null);

        URI parsedUri = getSingleURI();

        if (parsedUri != null) {
            parseAndSetParametersFromURI(parsedUri);
        } else {
            this.driverName = Drivers.EMBEDDED.driverClassName();
        }

        if (builder.username != null && builder.password != null) {
            if (this.credentials != null) {
                LOGGER.warn("Overriding credentials supplied in URI with supplied username and password.");
            }
            credentials = new UsernamePasswordCredentials(builder.username, builder.password);
        }
    }

    private void parseAndSetParametersFromURI(URI parsedUri) {
        String userInfo = parsedUri.getUserInfo();
        if (userInfo != null) {
            String[] userPass = userInfo.split(":");
            credentials = new UsernamePasswordCredentials(userPass[0], userPass[1]);
            this.uri = parsedUri.toString().replace(parsedUri.getUserInfo() + "@", "");
        }
        if (getDriverClassName() == null) {
            this.driverName = Drivers.getDriverFor(parsedUri.getScheme()).driverClassName();
        }
    }

    private URI getSingleURI() {
        try {

            if (uri != null) {
                return new URI(uri);
            }
            if (uris != null && uris.length >= 1) {
                return new URI(uris[0]);
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not configure supplied URI in Configuration", e);
        }
        return null;
    }

    public AutoIndexMode getAutoIndex() {
        return autoIndex;
    }

    public String getDumpDir() {
        return generatedIndexesOutputDir;
    }

    public String getDumpFilename() {
        return generatedIndexesOutputFilename;
    }

    public String getURI() {
        return uri;
    }

    public String[] getURIS() {
        return uris;
    }

    public String getDriverClassName() {
        return driverName;
    }

    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    public String getEncryptionLevel() {
        return encryptionLevel;
    }

    public String getTrustStrategy() {
        return trustStrategy;
    }

    public String getTrustCertFile() {
        return trustCertFile;
    }

    public Integer getConnectionLivenessCheckTimeout() {
        return connectionLivenessCheckTimeout;
    }

    public Boolean getVerifyConnection() {
        return verifyConnection;
    }

    public String getNeo4jConfLocation() {
        return this.neo4jConfLocation;
    }

    public String getDatabase() {
        return database;
    }

    /**
     * @return True if current configuration is setup to use embedded HA.
     */
    public boolean isEmbeddedHA() {

        boolean isEmbeddedHA = false;
        if (this.neo4jConfLocation != null) {
            try {
                URL url = ConfigurationUtils.getResourceUrl(neo4jConfLocation);

                Properties neo4Properties = new Properties();
                neo4Properties.load(url.openStream());

                isEmbeddedHA = !"SINGLE".equalsIgnoreCase(neo4Properties.getProperty("dbms.mode", "SINGLE"));
            } catch (IOException e) {
                throw new UncheckedIOException("Could not load neo4j.conf at location " + neo4jConfLocation, e);
            }
        }

        return isEmbeddedHA;
    }

    public URL getResourceUrl(String resourceLocation) throws FileNotFoundException {

        return ConfigurationUtils.getResourceUrl(resourceLocation);
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public Map<String, Object> getCustomProperties() {
        return Collections.unmodifiableMap(customProperties);
    }

    public Boolean getUseNativeTypes() {
        return useNativeTypes;
    }

    public Boolean getUseStrictQuerying() {
        return useStrictQuerying;
    }

    public String[] getBasePackages() {
        return basePackages;
    }

    public String[] mergeBasePackagesWith(String... anotherSetOfBasePackages) {
        String[] set1 = Optional.ofNullable(this.basePackages).orElseGet(() -> new String[0]);
        String[] set2 = Optional.ofNullable(anotherSetOfBasePackages).orElseGet(() -> new String[0]);

        return Stream.concat(Arrays.stream(set1), Arrays.stream(set2))
            .filter(s -> s != null)
            .distinct()
            .toArray(String[]::new);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Configuration)) {
            return false;
        }
        Configuration that = (Configuration) o;
        return connectionPoolSize == that.connectionPoolSize &&
            Objects.equals(uri, that.uri) &&
            Arrays.equals(uris, that.uris) &&
            Objects.equals(encryptionLevel, that.encryptionLevel) &&
            Objects.equals(trustStrategy, that.trustStrategy) &&
            Objects.equals(trustCertFile, that.trustCertFile) &&
            autoIndex == that.autoIndex &&
            Objects.equals(generatedIndexesOutputDir, that.generatedIndexesOutputDir) &&
            Objects.equals(generatedIndexesOutputFilename, that.generatedIndexesOutputFilename) &&
            Objects.equals(neo4jConfLocation, that.neo4jConfLocation) &&
            Objects.equals(driverName, that.driverName) &&
            Objects.equals(credentials, that.credentials) &&
            Objects.equals(connectionLivenessCheckTimeout, that.connectionLivenessCheckTimeout) &&
            Objects.equals(verifyConnection, that.verifyConnection) &&
            Objects.equals(useNativeTypes, that.useNativeTypes) &&
            Arrays.equals(basePackages, that.basePackages) &&
            Objects.equals(useStrictQuerying, that.useStrictQuerying);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(uri, connectionPoolSize, encryptionLevel, trustStrategy, trustCertFile, autoIndex,
            generatedIndexesOutputDir, generatedIndexesOutputFilename, neo4jConfLocation, driverName, credentials,
            connectionLivenessCheckTimeout, verifyConnection, useNativeTypes);
        result = 31 * result + Arrays.hashCode(uris);
        result = 31 * result + Arrays.hashCode(basePackages);
        return result;
    }

    /**
     * Builder for {@link Configuration} class
     */
    @SuppressWarnings("HiddenField")
    public static class Builder {

        // Those are the keys inside ogm.properties, not configuration values.
        private static final String URI = "URI";
        private static final String URIS = "URIS";
        private static final String USERNAME = "username";
        private static final String PASSWORD = "password";
        private static final String CONNECTION_POOL_SIZE = "connection.pool.size";
        private static final String ENCRYPTION_LEVEL = "encryption.level";
        private static final String TRUST_STRATEGY = "trust.strategy";
        private static final String TRUST_CERT_FILE = "trust.certificate.file";
        private static final String CONNECTION_LIVENESS_CHECK_TIMEOUT = "connection.liveness.check.timeout";
        private static final String VERIFY_CONNECTION = "verify.connection";
        private static final String AUTO_INDEX = "indexes.auto";
        private static final String GENERATED_INDEXES_OUTPUT_DIR = "indexes.auto.dump.dir";
        private static final String GENERATED_INDEXES_OUTPUT_FILENAME = "indexes.auto.dump.filename";
        private static final String NEO4J_CONF_LOCATION = "neo4j.conf.location";
        private static final String USE_NATIVE_TYPES = "use-native-types";
        private static final String BASE_PACKAGES = "base-packages";
        private static final String USE_STRICT_QUERYING = "use-strict-querying";
        private static final String DATABASE = "database";
        private String uri;
        private String[] uris;
        private Integer connectionPoolSize;
        private String encryptionLevel;
        private String trustStrategy;
        private String trustCertFile;
        private Integer connectionLivenessCheckTimeout;
        private Boolean verifyConnection;
        private String autoIndex;
        private String generatedIndexesOutputDir;
        private String generatedIndexesOutputFilename;
        private String neo4jConfLocation;
        private String username;
        private String password;
        private boolean useNativeTypes;
        private Map<String, Object> customProperties = new HashMap<>();
        private String[] basePackages;
        private boolean useStrictQuerying = true;
        private String database;

        /**
         * Creates new Configuration builder
         * Use for Java configuration.
         */
        public Builder() {
        }

        /**
         * Creates new Configuration builder
         *
         * @param configurationSource source of the configuration, file on classpath or filesystem
         */
        public Builder(ConfigurationSource configurationSource) {
            for (Map.Entry<Object, Object> entry : configurationSource.properties().entrySet()) {
                final String value = (String) entry.getValue();
                switch (entry.getKey().toString()) {
                    case URI:
                        this.uri = value;
                        break;
                    case USERNAME:
                        this.username = value;
                        break;
                    case PASSWORD:
                        this.password = value;
                        break;
                    case URIS:
                        this.uris = splitValue(entry.getValue());
                        break;
                    case CONNECTION_POOL_SIZE:
                        this.connectionPoolSize = Integer.parseInt(value);
                        break;
                    case ENCRYPTION_LEVEL:
                        this.encryptionLevel = value;
                        break;
                    case TRUST_STRATEGY:
                        this.trustStrategy = value;
                        break;
                    case TRUST_CERT_FILE:
                        this.trustCertFile = value;
                        break;
                    case CONNECTION_LIVENESS_CHECK_TIMEOUT:
                        this.connectionLivenessCheckTimeout = Integer.valueOf(value);
                        break;
                    case VERIFY_CONNECTION:
                        this.verifyConnection = Boolean.valueOf(value);
                        break;
                    case AUTO_INDEX:
                        this.autoIndex = value;
                        break;
                    case GENERATED_INDEXES_OUTPUT_DIR:
                        this.generatedIndexesOutputDir = value;
                        break;
                    case GENERATED_INDEXES_OUTPUT_FILENAME:
                        this.generatedIndexesOutputFilename = value;
                        break;
                    case NEO4J_CONF_LOCATION:
                        this.neo4jConfLocation = value;
                        break;
                    case USE_NATIVE_TYPES:
                        this.useNativeTypes = Boolean.valueOf(value);
                        break;
                    case BASE_PACKAGES:
                        this.basePackages = splitValue(entry.getValue());
                        break;
                    case USE_STRICT_QUERYING:
                        if (!(value == null || value.isEmpty())) {
                            this.useStrictQuerying = Boolean.valueOf(value);
                        }
                        break;
                    case DATABASE:
                        if (value != null && !value.trim().isEmpty()) {
                            this.database = value.trim();
                        }
                        break;
                    default:
                        LOGGER.warn("Could not process property with key: {}", entry.getKey());
                }
            }
        }

        public static Builder copy(Builder builder) {
            Builder copiedBuilder = new Builder()
                .uri(builder.uri)
                .connectionPoolSize(builder.connectionPoolSize)
                .encryptionLevel(builder.encryptionLevel)
                .trustStrategy(builder.trustStrategy)
                .trustCertFile(builder.trustCertFile)
                .connectionLivenessCheckTimeout(builder.connectionLivenessCheckTimeout)
                .verifyConnection(builder.verifyConnection)
                .autoIndex(builder.autoIndex)
                .generatedIndexesOutputDir(builder.generatedIndexesOutputDir)
                .generatedIndexesOutputFilename(builder.generatedIndexesOutputFilename)
                .neo4jConfLocation(builder.neo4jConfLocation)
                .credentials(builder.username, builder.password)
                .database(builder.database)
                .customProperties(new HashMap<>(builder.customProperties));

            if (builder.useStrictQuerying) {
                copiedBuilder.strictQuerying();
            } else {
                copiedBuilder.relaxedQuerying();
            }
            return copiedBuilder;
        }

        private static String[] splitValue(Object value) {

            if (!(value instanceof String)) {
                throw new IllegalArgumentException(
                    "Cannot split values of type other than java.lang.String (was " + value.getClass() + ").");
            }

            String stringValue = (String) value;
            if (stringValue == null || stringValue.trim().isEmpty()) {
                return new String[0];
            }

            return Arrays.stream(stringValue.split(","))
                .map(String::trim).toArray(String[]::new);
        }

        /**
         * Set URI of the database.
         * The driver is determined from the URI based on its scheme (http/https for HttpDriver,
         * file for EmbeddedDriver,
         * bolt for BoltDriver).
         *
         * @param uri uri of the database
         * @return the changed builder
         */
        public Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        /**
         * Set additional URIS to connect to causal cluster. All URIs must have bolt+routing scheme
         * (including one specified in uri property)
         *
         * @param uris uris
         * @return the changed builder
         */
        public Builder uris(String[] uris) {
            this.uris = uris;
            return this;
        }

        /**
         * Number of connections to the database.
         * Valid only for http and bolt drivers
         *
         * @param connectionPoolSize number of connections to the database
         * @return the changed builder
         */
        public Builder connectionPoolSize(Integer connectionPoolSize) {
            this.connectionPoolSize = connectionPoolSize;
            return this;
        }

        /**
         * Required encryption level for the connection to the database.
         * Possible values are  {@literal REQUIRED}, {@literal OPTIONAL}, {@literal DISABLED}.
         *
         * @param encryptionLevel required encryption level
         * @return the changed builder
         */
        public Builder encryptionLevel(String encryptionLevel) {
            this.encryptionLevel = encryptionLevel;
            return this;
        }

        public Builder trustStrategy(String trustStrategy) {
            this.trustStrategy = trustStrategy;
            return this;
        }

        public Builder trustCertFile(String trustCertFile) {
            this.trustCertFile = trustCertFile;
            return this;
        }

        public Builder connectionLivenessCheckTimeout(Integer connectionLivenessCheckTimeout) {
            this.connectionLivenessCheckTimeout = connectionLivenessCheckTimeout;
            return this;
        }

        /**
         * Whether OGM should verify connection to the database at creation of the Driver
         * Useful for "fail-fast" type of configuration where the database is expected to be running during application
         * start up and the connection to the database is expected to be very stable.
         * If the connection can't be verified {@link org.neo4j.ogm.exception.ConnectionException} will be thrown during
         * creation of SessionFactory.
         * If set to false the driver will be created when first Session is requested from SessionFactory
         *
         * @param verifyConnection if the connection to the database should be verified, default is false
         * @return the changed builder
         */
        public Builder verifyConnection(Boolean verifyConnection) {
            this.verifyConnection = verifyConnection;
            return this;
        }

        /**
         * Auto index config, for possible values see {@link org.neo4j.ogm.config.AutoIndexMode}
         *
         * @param autoIndex auto index config
         * @return the changed builder
         * @deprecated The usage of this tool is deprecated. Please use a proper migration tooling, like neo4j-migrations or liquibase with the Neo4j plugin. The build-in auto index manager only supports Neo4j 4.4 and higher.
         */
        @Deprecated
        public Builder autoIndex(String autoIndex) {
            this.autoIndex = autoIndex;
            return this;
        }

        public Builder generatedIndexesOutputDir(String generatedIndexesOutputDir) {
            this.generatedIndexesOutputDir = generatedIndexesOutputDir;
            return this;
        }

        public Builder generatedIndexesOutputFilename(String generatedIndexesOutputFilename) {
            this.generatedIndexesOutputFilename = generatedIndexesOutputFilename;
            return this;
        }

        public Builder neo4jConfLocation(String neo4jConfLocation) {
            this.neo4jConfLocation = neo4jConfLocation;
            return this;
        }

        private Builder customProperties(Map<String, Object> customProperties) {
            this.customProperties = customProperties;
            return this;
        }

        public Builder withCustomProperty(String name, Object value) {
            this.customProperties.put(name, value);
            return this;
        }

        /**
         * Turns on the support for native types on the transport level. All types supported natively by Neo4j will either
         * be transported "as is" to the database or in a format that will be stored in the native form in the database.
         * <br>
         * Turning this on prevents implicit conversions of all <code>java.time.*</code> types, Neo4j spatial datatypes
         * (<code>point()</code>) and potentially others in the future.
         * <br>
         * Be aware that turning this on in an application that used the implicit conversion and stored nodes and properties
         * with it, will require a refactoring to the database for all <code>java.time.*</code>-properties stored through
         * Neo4j-OGM: They have been stored traditionally as a string in an ISO-8601 format and need to be converted in the
         * database to their native representation as well.
         *
         * @return the changed builder
         * @since 3.2
         */
        public Builder useNativeTypes() {
            this.useNativeTypes = true;
            return this;
        }

        /**
         * Turns on strict querying. In strict querying mode, Neo4j-OGM uses all reachable static labels in a class inheritance
         * scenario when querying a domain object, either all, one by id oder all by ids. That is, in strict mode, a node
         * needs to have {@code n:LabelA:LabelB} when a domain class has this two labels due to inheritance. In relaxed mode,
         * the label of the concrete class is enough.
         * <p>
         * Turning strict mode on can improve query performance, when indexes are defined on labels spotted by parent classes.
         * <p>
         * Strict query mode is the default since 4.0.
         *
         * @return the changed builder
         * @since 3.1.16
         */
        public Builder strictQuerying() {
            this.useStrictQuerying = true;
            return this;
        }

        /**
         * Turns strict querying off and uses only the single static label of a domain class, even if this class is part
         * of an inheritance hierarchy exposing more than one static label. This may have impact on performance as indexes
         * may not be used. However, turning it off may be necessary to query nodes that have been created outside Neo4j-OGM
         * and are missing some labels.
         *
         * @return the changed builder
         * @since 3.1.16
         */
        public Builder relaxedQuerying() {
            this.useStrictQuerying = false;
            return this;
        }

        /**
         * Configures the builder with a list of base packages to scan.
         *
         * @param basePackages The new base backages.
         * @return the changed builder
         * @since 3.2
         */
        public Builder withBasePackages(String... basePackages) {
            this.basePackages = basePackages;
            return this;
        }

        /**
         * Configures the database to use. This is only applicable with the bolt transport connected against a
         * 4.0 database.
         *
         * @param database The default database to use, maybe {@literal null} but not empty. {@literal null} indicates
         *                 default database.
         * @return the changed builder
         * @since 3.2.6
         */
        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Configuration build() {
            return new Configuration(this);
        }

        /**
         * Credentials to use to access the database
         *
         * @param username username
         * @param password password
         * @return the changed builder
         */
        public Builder credentials(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }
    }
}
