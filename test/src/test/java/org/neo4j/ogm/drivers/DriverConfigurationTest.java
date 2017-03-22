/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

import static org.junit.Assert.*;

import org.junit.Test;
import org.neo4j.ogm.config.ClasspathConfigurationSource;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.Credentials;
import org.neo4j.ogm.config.UsernamePasswordCredentials;

/**
 * @author vince
 */

public class DriverConfigurationTest {

	@Test
	public void shouldLoadHttpDriverConfigFromPropertiesFile() {
		Configuration driverConfig = new Configuration.Builder(new ClasspathConfigurationSource("http.driver.properties")).build();
		assertEquals("http://localhost:7474", driverConfig.getURI());
	}

	@Test
	public void shouldLoadEmbeddedDriverConfigFromPropertiesFile() {
		Configuration driverConfig = new Configuration.Builder(new ClasspathConfigurationSource("embedded.driver.properties")).build();
		assertEquals("file:///var/tmp/neo4j.db", driverConfig.getURI());
	}

	@Test
	public void shouldLoadBoltDriverConfigFromPropertiesFile() {
		Configuration driverConfig = new Configuration.Builder(new ClasspathConfigurationSource("bolt.driver.properties")).build();
		assertEquals("bolt://localhost", driverConfig.getURI());
		assertEquals(150, driverConfig.getConnectionPoolSize());
		assertEquals("NONE", driverConfig.getEncryptionLevel());
		assertEquals("TRUST_ON_FIRST_USE", driverConfig.getTrustStrategy());
		assertEquals("/tmp/cert", driverConfig.getTrustCertFile());
	}

	@Test
	public void shouldSetUsernameAndPasswordCredentialsForBoltProtocol() {
		String username = "neo4j";
		String password = "password";
		Configuration dbConfig = new Configuration.Builder().build();
		dbConfig.setURI("bolt://" + username + ":" + password + "@localhost");
		Credentials credentials = dbConfig.getCredentials();
		UsernamePasswordCredentials basic = (UsernamePasswordCredentials) credentials;
		assertNotNull(basic);
		assertEquals(username, basic.getUsername());
		assertEquals(password, basic.getPassword());
	}

	@Test
	public void shouldGetNeo4jHaPropertiesFileFromDriverConfiguration() {
		Configuration config = new Configuration.Builder(new ClasspathConfigurationSource("embedded.ha.driver.properties")).build();
		assertEquals("neo4j-ha.properties", config.getNeo4jHaPropertiesFile());
	}
}
