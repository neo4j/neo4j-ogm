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

package org.neo4j.ogm.drivers;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.Credentials;
import org.neo4j.ogm.config.UsernamePasswordCredentials;

/**
 * @author vince
 * @author Michael J. Simons
 */
public class DriverConfigurationTest {

    @Test
    public void shouldLoadHttpDriverConfigFromPropertiesFile() {
        Configuration driverConfig = new Configuration.Builder(
            new ClasspathConfigurationSource("http.driver.properties")).build();
        assertThat(driverConfig.getURI()).isEqualTo("http://localhost:7474");
    }

    @Test
    public void shouldLoadEmbeddedDriverConfigFromPropertiesFile() {
        Configuration driverConfig = new Configuration.Builder(
            new ClasspathConfigurationSource("embedded.driver.properties")).build();
        assertThat(driverConfig.getURI()).isEqualTo("file:///var/tmp/neo4j.db");
    }

    @Test
    public void shouldLoadBoltDriverConfigFromPropertiesFile() {
        Configuration driverConfig = new Configuration.Builder(
            new ClasspathConfigurationSource("bolt.driver.properties")).build();
        assertThat(driverConfig.getURI()).isEqualTo("bolt://localhost");
        assertThat(driverConfig.getConnectionPoolSize()).isEqualTo(150);
        assertThat(driverConfig.getEncryptionLevel()).isEqualTo("NONE");
        assertThat(driverConfig.getTrustStrategy()).isEqualTo("TRUST_ON_FIRST_USE");
        assertThat(driverConfig.getTrustCertFile()).isEqualTo("/tmp/cert");
    }

    @Test
    public void shouldSetUsernameAndPasswordCredentialsForBoltProtocol() {
        String username = "neo4j";
        String password = "password";
        Configuration dbConfig = new Configuration.Builder().uri("bolt://" + username + ":" + password + "@localhost")
            .build();
        Credentials credentials = dbConfig.getCredentials();
        UsernamePasswordCredentials basic = (UsernamePasswordCredentials) credentials;
        assertThat(basic).isNotNull();
        assertThat(basic.getUsername()).isEqualTo(username);
        assertThat(basic.getPassword()).isEqualTo(password);
    }

    @Test
    public void shouldGetNeo4jHaPropertiesFileFromDriverConfiguration() {
        Configuration config = new Configuration.Builder(
            new ClasspathConfigurationSource("embedded.ha.driver.properties")).build();
        assertThat(config.getNeo4jConfLocation()).isEqualTo("neo4j-ha.properties");
    }
}
