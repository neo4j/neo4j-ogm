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

package org.neo4j.ogm.config;

import static org.junit.Assert.*;

import org.junit.Test;
import org.neo4j.ogm.session.Neo4jException;

/**
 * @author vince
 */
public class ConfigurationTest {

	@Test
	public void shouldConfigureProgrammatically() {

		Configuration.Builder builder = new Configuration.Builder();

		builder.autoIndex("assert");
		builder.generatedIndexesOutputDir("dir");
		builder.generatedIndexesOutputFilename("filename");
		builder.credentials("fred", "flintstone");
		builder.uri("http://localhost:8080");
		builder.connectionPoolSize(200);
		builder.encryptionLevel("REQUIRED");
		builder.trustStrategy("TRUST_SIGNED_CERTIFICATES");
		builder.trustCertFile("/tmp/cert");

		Configuration configuration = builder.build();

		assertEquals(AutoIndexMode.ASSERT, configuration.getAutoIndex());
		assertEquals("dir", configuration.getDumpDir());
		assertEquals("filename", configuration.getDumpFilename());
		assertEquals("org.neo4j.ogm.drivers.http.driver.HttpDriver", configuration.getDriverClassName());
		assertEquals("ZnJlZDpmbGludHN0b25l", configuration.getCredentials().credentials().toString());
		assertEquals("http://localhost:8080", configuration.getURI());
		assertEquals(200, configuration.getConnectionPoolSize());
		assertEquals("REQUIRED", configuration.getEncryptionLevel());
		assertEquals("TRUST_SIGNED_CERTIFICATES", configuration.getTrustStrategy());
		assertEquals("/tmp/cert", configuration.getTrustCertFile());
	}

	@Test
	public void shouldConfigureCredentialsFromURI() {
		Configuration configuration = new Configuration.Builder().uri("http://fred:flintstone@localhost:8080").build();
		assertEquals("ZnJlZDpmbGludHN0b25l", configuration.getCredentials().credentials().toString());
	}

	@Test
	public void shouldConfigureCredentialsFromURIWithUTF8Charactes() {
		Configuration configuration = new Configuration.Builder()
				.uri("http://franti\u0161ek:Pass123@localhost:8080")
				.build();

		assertEquals("ZnJhbnRpxaFlazpQYXNzMTIz", configuration.getCredentials().credentials().toString());
	}

	@Test
	public void shouldConfigureFromSimplePropertiesFile() {
		Configuration configuration = new Configuration.Builder(new ClasspathConfigurationSource("ogm-simple.properties")).build();

		assertEquals(AutoIndexMode.NONE, configuration.getAutoIndex());
		assertEquals("org.neo4j.ogm.drivers.http.driver.HttpDriver", configuration.getDriverClassName());
		assertEquals("bmVvNGo6cGFzc3dvcmQ=", configuration.getCredentials().credentials().toString());
		assertEquals("http://localhost:7474", configuration.getURI());
	}

	@Test(expected = RuntimeException.class)
	public void uriWithNoScheme() {
		Configuration configuration = new Configuration.Builder().uri("target/noe4j/my.db").build();
		fail("Should have thrown a runtime exception about a missing URI Scheme");
	}

	@Test
	public void shouldConfigureFromNameSpacePropertiesFile() {

		Configuration configuration = new Configuration.Builder(new ClasspathConfigurationSource("ogm-namespace.properties")).build();

		assertEquals(AutoIndexMode.DUMP, configuration.getAutoIndex());
		assertEquals("hello", configuration.getDumpDir());
		assertEquals("generated-indexes2.cql", configuration.getDumpFilename());
		assertEquals("org.neo4j.ogm.drivers.http.driver.HttpDriver", configuration.getDriverClassName());
		assertEquals("bmVvNGo6cGFzc3dvcmQ=", configuration.getCredentials().credentials().toString());
		assertEquals("http://localhost:7474", configuration.getURI());
		assertEquals(100, configuration.getConnectionPoolSize());
		assertEquals("NONE", configuration.getEncryptionLevel());
		assertEquals("TRUST_ON_FIRST_USE", configuration.getTrustStrategy());
		assertEquals("/tmp/cert", configuration.getTrustCertFile());
	}

	@Test
	public void shouldConfigureFromSpringBootPropertiesFile() {

		Configuration configuration = new Configuration.Builder(new ClasspathConfigurationSource("application.properties")).build();

		assertEquals(AutoIndexMode.NONE, configuration.getAutoIndex());
		assertEquals("org.neo4j.ogm.drivers.http.driver.HttpDriver", configuration.getDriverClassName());
		assertEquals("bmVvNGo6cGFzc3dvcmQ=", configuration.getCredentials().credentials().toString());
		assertEquals("http://localhost:7474", configuration.getURI());
	}
}
