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

package org.neo4j.ogm.config;

import static org.neo4j.ogm.driver.ParameterConversionMode.*;

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
import java.util.Properties;

import org.neo4j.ogm.driver.ParameterConversionMode;
import org.neo4j.ogm.support.ResourceUtils;
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
     * Protected constructor of the Configuration class.
     * Use {@link Builder} to create an instance of Configuration class
     */
    Configuration(Builder builder) {
        this.uri = builder.uri;
        this.uris = builder.uris;
        this.connectionPoolSize = builder.connectionPoolSize != null ? builder.connectionPoolSize : 50;
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

        this.assertNativeTypeRequirements();

        if (this.uri != null) {
            java.net.URI uri = null;
            try {
                uri = new URI(this.uri);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Could not configure supplied URI in Configuration", e);
            }
            String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                String[] userPass = userInfo.split(":");
                credentials = new UsernamePasswordCredentials(userPass[0], userPass[1]);
                this.uri = uri.toString().replace(uri.getUserInfo() + "@", "");
            }
            if (getDriverClassName() == null) {
                this.driverName = Drivers.getDriverFor(uri.getScheme()).driverClassName();
            }
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

    /**
     * Asserts additional requirements for the native type support, i.e. using the "modern" parameter conversion.
     */
    private void assertNativeTypeRequirements() {

        if(!this.useNativeTypes)
            return;

        ParameterConversionMode currentParameterConversionMode = (ParameterConversionMode) this.customProperties
            .get(CONFIG_PARAMETER_CONVERSION_MODE);

        if(currentParameterConversionMode != null && currentParameterConversionMode != CONVERT_NON_NATIVE_ONLY) {
            throw new IllegalStateException("Cannot use native types, parameter conversion mode has been explicitly set to " + CONVERT_ALL);
        }

        this.customProperties.put(CONFIG_PARAMETER_CONVERSION_MODE, CONVERT_NON_NATIVE_ONLY);
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

    /**
     * @return True if current configuration is setup to use embedded HA.
     */
    public boolean isEmbeddedHA() {

        boolean isEmbeddedHA = false;
        if (this.neo4jConfLocation != null) {
            try {
                URL url = ResourceUtils.getResourceUrl(neo4jConfLocation);

                Properties neo4Properties = new Properties();
                neo4Properties.load(url.openStream());

                isEmbeddedHA = "HA".equalsIgnoreCase(neo4Properties.getProperty("dbms.mode", "-"));
            } catch (IOException e) {
                throw new UncheckedIOException("Could not load neo4j.conf at location " + neo4jConfLocation, e);
            }
        }

        return isEmbeddedHA;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Configuration))
            return false;
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
            Objects.equals(verifyConnection, that.verifyConnection);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(uri, connectionPoolSize, encryptionLevel, trustStrategy, trustCertFile, autoIndex,
            generatedIndexesOutputDir, generatedIndexesOutputFilename, neo4jConfLocation, driverName,
            credentials, connectionLivenessCheckTimeout, verifyConnection);
        result = 31 * result + Arrays.hashCode(uris);
        return result;
    }

    /**
     * Builder for {@link Configuration} class
     */
    public static class Builder {

        public static Builder copy(Builder builder) {
            return new Builder()
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
                .customProperties(new HashMap<>(builder.customProperties));
        }

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
                switch (entry.getKey().toString()) {
                    case URI:
                        this.uri = (String) entry.getValue();
                        break;
                    case USERNAME:
                        this.username = (String) entry.getValue();
                        break;
                    case PASSWORD:
                        this.password = (String) entry.getValue();
                        break;
                    case URIS:
                        this.uris = ((String) entry.getValue()).split(",");
                        break;
                    case CONNECTION_POOL_SIZE:
                        this.connectionPoolSize = Integer.parseInt((String) entry.getValue());
                        break;
                    case ENCRYPTION_LEVEL:
                        this.encryptionLevel = (String) entry.getValue();
                        break;
                    case TRUST_STRATEGY:
                        this.trustStrategy = (String) entry.getValue();
                        break;
                    case TRUST_CERT_FILE:
                        this.trustCertFile = (String) entry.getValue();
                        break;
                    case CONNECTION_LIVENESS_CHECK_TIMEOUT:
                        this.connectionLivenessCheckTimeout = Integer.valueOf((String) entry.getValue());
                        break;
                    case VERIFY_CONNECTION:
                        this.verifyConnection = Boolean.valueOf((String) entry.getValue());
                        break;
                    case AUTO_INDEX:
                        this.autoIndex = (String) entry.getValue();
                        break;
                    case GENERATED_INDEXES_OUTPUT_DIR:
                        this.generatedIndexesOutputDir = (String) entry.getValue();
                        break;
                    case GENERATED_INDEXES_OUTPUT_FILENAME:
                        this.generatedIndexesOutputFilename = (String) entry.getValue();
                        break;
                    case NEO4J_CONF_LOCATION:
                        this.neo4jConfLocation = (String) entry.getValue();
                        break;
                    default:
                        LOGGER.warn("Could not process property with key: {}", entry.getKey());
                }
            }
        }

        /**
         * Set URI of the database.
         * The driver is determined from the URI based on its scheme (http/https for HttpDriver,
         * file for EmbeddedDriver,
         * bolt for BoltDriver).
         *
         * @param uri uri of the database
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
         */
        public Builder connectionPoolSize(Integer connectionPoolSize) {
            this.connectionPoolSize = connectionPoolSize;
            return this;
        }

        /**
         * Required encryption level for the connection to the database.
         * See org.neo4j.driver.v1.Config.EncryptionLevel for possible values.
         *
         * @param encryptionLevel required encryption level
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
         */
        public Builder verifyConnection(Boolean verifyConnection) {
            this.verifyConnection = verifyConnection;
            return this;
        }

        /**
         * Auto index config, for possible values see {@link org.neo4j.ogm.config.AutoIndexMode}
         *
         * @param autoIndex auto index config
         */
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
         * <br />
         * Turning this on prevents implicit conversions of all <code>java.time.*</code> types, Neo4j spatial datatypes
         * (<code>point()</code>) and potentially others in the future.
         * <br />
         * Be aware that turning this on in an application that used the implicit conversion and stored nodes and properties
         * with it, will require a refactoring to the database for all <code>java.time.*</code>-properties stored through
         * Neo4j-OGM: They have been stored traditionally as a string in an ISO-8601 format and need to be converted in the
         * database to their native representation as well.
         *
         * @since 3.2
         */
        public Builder useNativeTypes() {
            this.useNativeTypes = true;
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
         */
        public Builder credentials(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }
    }
}
