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
