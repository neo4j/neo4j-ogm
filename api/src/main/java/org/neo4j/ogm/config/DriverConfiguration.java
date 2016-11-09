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

import org.neo4j.ogm.authentication.Credentials;
import org.neo4j.ogm.authentication.UsernamePasswordCredentials;

/**
 *
 * A wrapper class for a generic {@link Configuration} that exposes the configuration for a Driver via
 * concrete methods.
 *
 * @author vince
 */
public class DriverConfiguration {

    public static final String[] DRIVER = {"neo4j.ogm.driver","spring.data.neo4j.driver", "driver"};
    public static final String[] CREDENTIALS = {"neo4j.ogm.credentials","spring.data.neo4j.credentials", "credentials"};
    public static final String[] URI = {"neo4j.ogm.URI", "spring.data.neo4j.URI", "URI"};
    public static final String[] USERNAME = {"neo4j.ogm.username", "spring.data.neo4j.username", "username"};
    public static final String[] PASSWORD = {"neo4j.ogm.password", "spring.data.neo4j.password", "password"};

    public static final String[] CONNECTION_POOL_SIZE   = {"connection.pool.size"};
    public static final String[] ENCRYPTION_LEVEL       = {"encryption.level"};
    public static final String[] TRUST_STRATEGY         = {"trust.strategy"};
    public static final String[] TRUST_CERT_FILE        = {"trust.certificate.file"};

	public static final String[] NEO4J_HA_PROPERTIES_FILE = {"neo4j.ha.properties.file"};

    // defaults
    private static final int CONNECTION_POOL_SIZE_DEFAULT     = 50;

    private final Configuration configuration;

    public DriverConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public DriverConfiguration setDriverClassName(String driverClassName) {
        configuration.set(DRIVER[0], driverClassName);
        return this;
    }

    public DriverConfiguration setURI(String uri) {
        configuration.set(URI[0], uri);
        try { // if this URI is a genuine resource, see if it has an embedded user-info and set credentials accordingly
            URI url = new URI(uri);
            String userInfo = url.getUserInfo();
            if (userInfo != null) {
                String[] userPass = userInfo.split(":");
                setCredentials(userPass[0], userPass[1]);
            }
            if (configuration.driverConfiguration().getDriverClassName() == null) {
                determineDefaultDriverName(url);
            }
        } catch (Exception e) {
            // do nothing here. user not obliged to supply a URL, or to pass in credentials
        }
        return this;
    }

    public DriverConfiguration setCredentials(Credentials credentials) {
        configuration.set(CREDENTIALS[0], credentials);
        return this;
    }

    public DriverConfiguration setCredentials(String username, String password) {
        configuration.set(CREDENTIALS[0], new UsernamePasswordCredentials(username, password));
        return this;
    }

    public DriverConfiguration setConnectionPoolSize(Integer sessionPoolSize) {
        configuration.set(CONNECTION_POOL_SIZE[0], sessionPoolSize.toString());
        return this;
    }

    public DriverConfiguration setEncryptionLevel(String encryptionLevel) {
        configuration.set(ENCRYPTION_LEVEL[0], encryptionLevel);
        return this;
    }

    public DriverConfiguration setTrustStrategy(String trustStrategy) {
        configuration.set(TRUST_STRATEGY[0], trustStrategy);
        return this;
    }

    public DriverConfiguration setTrustCertFile(String trustCertFile) {
        configuration.set(TRUST_CERT_FILE[0], trustCertFile);
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

        if (configuration.get(CREDENTIALS) == null) {
            setURI((String) configuration.get(URI)); // set from the URI?
        }

        if (configuration.get(CREDENTIALS) == null) {
            String username = (String) configuration.get(USERNAME);
            String password = (String) configuration.get(PASSWORD);
            if (username != null && password != null) {
                setCredentials(username, password); // set from username/password pair
            }
        }
        return (Credentials) configuration.get(CREDENTIALS);
    }

    public String getURI() {
        return (String)  configuration.get(URI);
    }

    public String getDriverClassName() {
        return (String) configuration.get(DRIVER);
    }

    public Integer getConnectionPoolSize() {
        if (configuration.get(CONNECTION_POOL_SIZE) != null) {
            return Integer.valueOf((String)configuration.get(CONNECTION_POOL_SIZE));
        }
        return CONNECTION_POOL_SIZE_DEFAULT;
    }

    public String getEncryptionLevel() {
        if (configuration.get(ENCRYPTION_LEVEL) != null) {
            return (String)configuration.get(ENCRYPTION_LEVEL);
        }
        return null;
    }

    public String getTrustStrategy() {
        if (configuration.get(TRUST_STRATEGY) != null) {
            return (String)configuration.get(TRUST_STRATEGY);
        }
        return null;
    }

    public String getTrustCertFile() {
        if (configuration.get(TRUST_CERT_FILE) != null) {
            return (String)configuration.get(TRUST_CERT_FILE);
        }
        return null;
    }

    public String getNeo4jHaPropertiesFile() {
		if (configuration.get(NEO4J_HA_PROPERTIES_FILE) != null) {
			return (String) configuration.get(NEO4J_HA_PROPERTIES_FILE);
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
