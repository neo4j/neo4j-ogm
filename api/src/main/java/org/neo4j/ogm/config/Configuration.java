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

import java.net.URI;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic configuration class that can be set up programmatically
 * or via a properties file.
 *
 * @author vince
 */
public class Configuration {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private static final String DRIVER = "driver";
    private static final String URI = "URI";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CONNECTION_POOL_SIZE = "connection.pool.size";
    private static final String ENCRYPTION_LEVEL = "encryption.level";
    private static final String TRUST_STRATEGY = "trust.strategy";
    private static final String TRUST_CERT_FILE = "trust.certificate.file";
    private static final String AUTO_INDEX = "indexes.auto";
    private static final String GENERATED_INDEXES_OUTPUT_DIR = "indexes.auto.dump.dir";
    private static final String GENERATED_INDEXES_OUTPUT_FILENAME = "indexes.auto.dump.filename";
    private static final String NEO4J_HA_PROPERTIES_FILE = "neo4j.ha.properties.file";

    private final Properties properties;
    private Credentials credentials;

    public Configuration() {
        properties = new Properties();
    }

    public Configuration(ConfigurationSource configurationSource) {
        properties = configurationSource.properties();
        try {
            java.net.URI url = new URI(properties.getProperty(URI));
            String userInfo = url.getUserInfo();
            if (userInfo != null) {
                String[] userPass = userInfo.split(":");
                setCredentials(userPass[0], userPass[1]);
                properties.setProperty(URI, url.toString().replace(url.getUserInfo() + "@", ""));
            }
        } catch (Exception e) {
            // do nothing here. user not obliged to supply a URL, or to pass in credentials
        }
    }

    public void clear() {
        properties.clear();
    }

    public String getAutoIndex() {
        return properties.getProperty(AUTO_INDEX, "none");
    }

    public String getDumpDir() {
        return properties.getProperty(GENERATED_INDEXES_OUTPUT_DIR, ".");
    }

    public String getDumpFilename() {
        return properties.getProperty(GENERATED_INDEXES_OUTPUT_FILENAME, "generated_indexes.cql");
    }

    public String getURI() {
        return properties.getProperty(URI);
    }

    public String getDriverClassName() {
        return properties.getProperty(DRIVER);
    }

    public String getUsername() {
        return properties.getProperty(USERNAME);
    }

    public String getPassword() {
        return properties.getProperty(PASSWORD);
    }

    public Integer getConnectionPoolSize() {
        return Integer.valueOf(properties.getProperty(CONNECTION_POOL_SIZE, "50"));
    }

    public String getEncryptionLevel() {
        return properties.getProperty(ENCRYPTION_LEVEL);
    }

    public String getTrustStrategy() {
        return properties.getProperty(TRUST_STRATEGY);
    }

    public String getTrustCertFile() {
        return properties.getProperty(TRUST_CERT_FILE);
    }

    public String getNeo4jHaPropertiesFile() {
        return properties.getProperty(NEO4J_HA_PROPERTIES_FILE);
    }

    public Credentials getCredentials() {
        return credentials;
    }

    // SETTERS - TODO: Move to builder.

    public Configuration setAutoIndex(String value) {
        properties.put(AUTO_INDEX, value);
        return this;
    }

    public Configuration setDumpDir(String dumpDir) {
        properties.put(GENERATED_INDEXES_OUTPUT_DIR, dumpDir);
        return this;
    }

    public Configuration setDumpFilename(String dumpFilename) {
        properties.put(GENERATED_INDEXES_OUTPUT_FILENAME, dumpFilename);
        return this;
    }

    public Configuration setDriverClassName(String driverClassName) {
        properties.put(DRIVER, driverClassName);
        return this;
    }

    public Configuration setURI(String uri) {
        if (uri == null) {
            properties.remove(URI);
            return this;
        }
        properties.put(URI, uri);
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


    public Configuration setConnectionPoolSize(Integer sessionPoolSize) {
        properties.put(CONNECTION_POOL_SIZE, sessionPoolSize.toString());
        return this;
    }

    public Configuration setEncryptionLevel(String encryptionLevel) {
        properties.put(ENCRYPTION_LEVEL, encryptionLevel);
        return this;
    }

    public Configuration setTrustStrategy(String trustStrategy) {
        properties.put(TRUST_STRATEGY, trustStrategy);
        return this;
    }

    public Configuration setTrustCertFile(String trustCertFile) {
        properties.put(TRUST_CERT_FILE, trustCertFile);
        return this;
    }

    public void setCredentials(String username, String password) {
        credentials = new UsernamePasswordCredentials(username, password);
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
