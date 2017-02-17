/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

import java.io.InputStream;
import java.net.URI;
import java.util.*;

import org.neo4j.ogm.classloader.ClassLoaderResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic configuration class that can be set up programmatically
 * or via a properties file.
 *
 * @author vince
 */
public class Configuration implements AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(Configuration.class);

    public static final String[] DRIVER = {"neo4j.ogm.driver", "spring.data.neo4j.driver", "driver"};
    public static final String[] CREDENTIALS = {"neo4j.ogm.credentials", "spring.data.neo4j.credentials", "credentials"};
    public static final String[] URI = {"neo4j.ogm.URI", "spring.data.neo4j.URI", "URI"};
    public static final String[] USERNAME = {"neo4j.ogm.username", "spring.data.neo4j.username", "username"};
    public static final String[] PASSWORD = {"neo4j.ogm.password", "spring.data.neo4j.password", "password"};
    public static final String[] CONNECTION_POOL_SIZE = {"connection.pool.size"};
    public static final String[] ENCRYPTION_LEVEL = {"encryption.level"};
    public static final String[] TRUST_STRATEGY = {"trust.strategy"};
    public static final String[] TRUST_CERT_FILE = {"trust.certificate.file"};
    public static final String[] AUTO_INDEX = {"neo4j.ogm.indexes.auto", "indexes.auto"};
    public static final String[] GENERATED_INDEXES_OUTPUT_DIR = {"neo4j.ogm.indexes.auto.dump.dir", "indexes.auto.dump.dir"};
    public static final String[] GENERATED_INDEXES_OUTPUT_FILENAME = {"neo4j.ogm.indexes.auto.dump.filename", "indexes.auto.dump.filename"};
    public static final String[] NEO4J_HA_PROPERTIES_FILE = {"neo4j.ha.properties.file"};

    // defaults
    private static final int CONNECTION_POOL_SIZE_DEFAULT = 50;
    private static final AutoIndexMode DEFAULT_AUTO_INDEX_VALUE = AutoIndexMode.NONE;
    private static final String DEFAULT_GENERATED_INDEXES_FILENAME = "generated_indexes.cql";
    private static final String DEFAULT_GENERATED_INDEXES_DIR = ".";

    private final Map<String, Object> config = new HashMap<>();


    public Configuration() {
    }

    public Configuration(String propertiesFilename) {
        configure(propertiesFilename);
    }

    public void set(String key, Object value) {
        config.put(key, value);
    }

    public Object get(String key) {
        return config.get(key);
    }

    public Object get(String... keys) {
        for (String key : keys) {
            Object obj = config.get(key);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

    private void configure(String propertiesFileName) {

        try (InputStream is = ClassLoaderResolver.resolve().getResourceAsStream(propertiesFileName)) {

            Properties properties = new Properties();
            properties.load(is);
            Enumeration propertyNames = properties.propertyNames();

            while (propertyNames.hasMoreElements()) {
                String propertyName = (String) propertyNames.nextElement();
                config.put(propertyName, properties.getProperty(propertyName));
            }
        } catch (Exception e) {
            logger.warn("Could not load {}", propertiesFileName);
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(" {\n");
        for (Map.Entry entry : config.entrySet()) {
            sb.append("\t");
            sb.append(entry.getKey());
            sb.append("='");
            sb.append(entry.getValue());
            sb.append("'");
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public void clear() {
        config.clear();
    }

    public void close() {
        Components.destroy();
    }


    // AUTO INDEX CONFIG
    // =================

    public Configuration setAutoIndex(String value) {

        if (AutoIndexMode.fromString(value) == null) {
            throw new RuntimeException("Invalid index value: " + value + ". Value must be one of: " + Arrays.toString(AutoIndexMode.values()));
        }

        set(AUTO_INDEX[0], value);
        return this;
    }

    public AutoIndexMode getAutoIndex() {
        if (get(AUTO_INDEX) == null) {
            return DEFAULT_AUTO_INDEX_VALUE;
        }
        return AutoIndexMode.fromString((String) get(AUTO_INDEX));
    }


    public Configuration setDumpDir(String dumpDir) {
        set(GENERATED_INDEXES_OUTPUT_DIR[0], dumpDir);
        return this;
    }

    public String getDumpDir() {
        if (get(GENERATED_INDEXES_OUTPUT_DIR) == null) {
            return DEFAULT_GENERATED_INDEXES_DIR;
        }
        return (String) get(GENERATED_INDEXES_OUTPUT_DIR);
    }

    public Configuration setDumpFilename(String dumpFilename) {
        set(GENERATED_INDEXES_OUTPUT_FILENAME[0], dumpFilename);
        return this;
    }

    public String getDumpFilename() {
        if (get(GENERATED_INDEXES_OUTPUT_FILENAME) == null) {
            return DEFAULT_GENERATED_INDEXES_FILENAME;
        }
        return (String) get(GENERATED_INDEXES_OUTPUT_FILENAME);
    }

    // DRIVER CONFIG
    // =============


    public Configuration setDriverClassName(String driverClassName) {
        set(DRIVER[0], driverClassName);
        return this;
    }

    public Configuration setURI(String uri) {
        set(URI[0], uri);
        try { // if this URI is a genuine resource, see if it has an embedded user-info and set credentials accordingly
            java.net.URI url = new URI(uri);
            String userInfo = url.getUserInfo();
            if (userInfo != null) {
                String[] userPass = userInfo.split(":");
                setCredentials(userPass[0], userPass[1]);
            }
            if (getDriverClassName() == null) {
                determineDefaultDriverName(url);
            }
        } catch (Exception e) {
            // do nothing here. user not obliged to supply a URL, or to pass in credentials
        }
        return this;
    }

    public Configuration setCredentials(Credentials credentials) {
        set(CREDENTIALS[0], credentials);
        return this;
    }

    public Configuration setCredentials(String username, String password) {
        set(CREDENTIALS[0], new UsernamePasswordCredentials(username, password));
        return this;
    }

    public Configuration setConnectionPoolSize(Integer sessionPoolSize) {
        set(CONNECTION_POOL_SIZE[0], sessionPoolSize.toString());
        return this;
    }

    public Configuration setEncryptionLevel(String encryptionLevel) {
        set(ENCRYPTION_LEVEL[0], encryptionLevel);
        return this;
    }

    public Configuration setTrustStrategy(String trustStrategy) {
        set(TRUST_STRATEGY[0], trustStrategy);
        return this;
    }

    public Configuration setTrustCertFile(String trustCertFile) {
        set(TRUST_CERT_FILE[0], trustCertFile);
        return this;
    }

    /**
     * Returns the driver connection credentials, if they have been provided.
     * If a Credentials object exists, it will be returned
     * If a Credentials object does not exist, it will be created from the URI's authentication part
     * If the URI does not contain an authentication part, Credentials will be created from username/password properties
     * if they have been set.
     *
     * @return a Credentials object if one exists or can be created, null otherwise
     */
    public Credentials getCredentials() {

        if (get(CREDENTIALS) == null) {
            setURI((String) get(URI)); // set from the URI?
        }

        if (get(CREDENTIALS) == null) {
            String username = (String) get(USERNAME);
            String password = (String) get(PASSWORD);
            if (username != null && password != null) {
                setCredentials(username, password); // set from username/password pair
            }
        }
        return (Credentials) get(CREDENTIALS);
    }

    public String getURI() {
        return (String)  get(URI);
    }

    public String getDriverClassName() {
        return (String) get(DRIVER);
    }

    public Integer getConnectionPoolSize() {
        if (get(CONNECTION_POOL_SIZE) != null) {
            return Integer.valueOf((String)get(CONNECTION_POOL_SIZE));
        }
        return CONNECTION_POOL_SIZE_DEFAULT;
    }

    public String getEncryptionLevel() {
        if (get(ENCRYPTION_LEVEL) != null) {
            return (String)get(ENCRYPTION_LEVEL);
        }
        return null;
    }

    public String getTrustStrategy() {
        if (get(TRUST_STRATEGY) != null) {
            return (String)get(TRUST_STRATEGY);
        }
        return null;
    }

    public String getTrustCertFile() {
        if (get(TRUST_CERT_FILE) != null) {
            return (String)get(TRUST_CERT_FILE);
        }
        return null;
    }

    public String getNeo4jHaPropertiesFile() {
        if (get(NEO4J_HA_PROPERTIES_FILE) != null) {
            return (String) get(NEO4J_HA_PROPERTIES_FILE);
        }
        return null;
    }

    private void determineDefaultDriverName(URI uri) {
        switch (uri.getScheme()) {
            case "http":
            case "https":
                setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver");
                break;
            case "bolt":
                setDriverClassName("org.neo4j.ogm.drivers.bolt.driver.BoltDriver");
                break;
            default:
                setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
                break;
        }
    }
}
