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

import java.io.IOException;
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
public class Configuration {

    private final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private static final String[] DRIVER = {"neo4j.ogm.driver", "spring.data.neo4j.driver", "driver"};
    private static final String[] CREDENTIALS = {"neo4j.ogm.credentials", "spring.data.neo4j.credentials", "credentials"};
    private static final String[] URI = {"neo4j.ogm.URI", "spring.data.neo4j.URI", "URI"};
    private static final String[] USERNAME = {"neo4j.ogm.username", "spring.data.neo4j.username", "username"};
    private static final String[] PASSWORD = {"neo4j.ogm.password", "spring.data.neo4j.password", "password"};
    private static final String[] CONNECTION_POOL_SIZE = {"connection.pool.size"};
    private static final String[] ENCRYPTION_LEVEL = {"encryption.level"};
    private static final String[] TRUST_STRATEGY = {"trust.strategy"};
    private static final String[] TRUST_CERT_FILE = {"trust.certificate.file"};
    private static final String[] AUTO_INDEX = {"neo4j.ogm.indexes.auto", "indexes.auto"};
    private static final String[] GENERATED_INDEXES_OUTPUT_DIR = {"neo4j.ogm.indexes.auto.dump.dir", "indexes.auto.dump.dir"};
    private static final String[] GENERATED_INDEXES_OUTPUT_FILENAME = {"neo4j.ogm.indexes.auto.dump.filename", "indexes.auto.dump.filename"};
    private static final String[] NEO4J_HA_PROPERTIES_FILE = {"neo4j.ha.properties.file"};
    private static final String[] NEO4J_VERSION = {"neo4j.version"};

    private final Map<String, Object> config = new HashMap<>();


    public Configuration() {
    }

    public Configuration(String propertiesFilename) {
        try (InputStream is = ClassLoaderResolver.resolve().getResourceAsStream(propertiesFilename)) {
            configure(is);
        } catch (Exception e) {
            logger.warn("Could not load {}", propertiesFilename);
        }
    }

    private Object get(String... keys) {
        for (String key : keys) {
            Object obj = config.get(key);
            if (obj != null) {
                return obj;
            }
        }
        return null;
    }

    public void configure(InputStream is) throws IOException {
        Properties properties = new Properties();
        properties.load(is);
        Enumeration propertyNames = properties.propertyNames();

        while (propertyNames.hasMoreElements()) {
            String propertyName = (String) propertyNames.nextElement();
            config.put(propertyName, properties.getProperty(propertyName));
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

    public Configuration setAutoIndex(String value) {

        if (AutoIndexMode.fromString(value) == null) {
            throw new RuntimeException("Invalid index value: " + value + ". Value must be one of: " + Arrays.toString(AutoIndexMode.values()));
        }

        config.put(AUTO_INDEX[0], value);
        return this;
    }

    public AutoIndexMode getAutoIndex() {
        if (get(AUTO_INDEX) == null) {
            return AutoIndexMode.NONE;
        }
        return AutoIndexMode.fromString((String) get(AUTO_INDEX));
    }


    public Configuration setDumpDir(String dumpDir) {
        config.put(GENERATED_INDEXES_OUTPUT_DIR[0], dumpDir);
        return this;
    }

    public String getDumpDir() {
        if (get(GENERATED_INDEXES_OUTPUT_DIR) == null) {
            return ".";
        }
        return (String) get(GENERATED_INDEXES_OUTPUT_DIR);
    }

    public Configuration setDumpFilename(String dumpFilename) {
        config.put(GENERATED_INDEXES_OUTPUT_FILENAME[0], dumpFilename);
        return this;
    }

    public String getDumpFilename() {
        if (get(GENERATED_INDEXES_OUTPUT_FILENAME) == null) {
            return "generated_indexes.cql";
        }
        return (String) get(GENERATED_INDEXES_OUTPUT_FILENAME);
    }

    // DRIVER CONFIG
    // =============


    public Configuration setDriverClassName(String driverClassName) {
        config.put(DRIVER[0], driverClassName);
        return this;
    }

    public Configuration setURI(String uri) {
        config.put(URI[0], uri);
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
        config.put(CREDENTIALS[0], credentials);
        return this;
    }

    public Configuration setCredentials(String username, String password) {
        config.put(CREDENTIALS[0], new UsernamePasswordCredentials(username, password));
        return this;
    }

    public Configuration setConnectionPoolSize(Integer sessionPoolSize) {
        config.put(CONNECTION_POOL_SIZE[0], sessionPoolSize.toString());
        return this;
    }

    public Configuration setEncryptionLevel(String encryptionLevel) {
        config.put(ENCRYPTION_LEVEL[0], encryptionLevel);
        return this;
    }

    public Configuration setTrustStrategy(String trustStrategy) {
        config.put(TRUST_STRATEGY[0], trustStrategy);
        return this;
    }

    public Configuration setTrustCertFile(String trustCertFile) {
        config.put(TRUST_CERT_FILE[0], trustCertFile);
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
        return 50;
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

    public String getNeo4jVersion() {
        return  (String) get(NEO4J_VERSION);
    }
}
