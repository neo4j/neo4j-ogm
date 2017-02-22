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

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @author vince
 */
public class ConfigurationTest {

    @Test
    public void shouldConfigureProgrammatically() {

        Configuration configuration = new Configuration();

        configuration.setAutoIndex("assert");
        configuration.setDumpDir("dir");
        configuration.setDumpFilename("filename");
        configuration.setDriverClassName("driver");
        configuration.setCredentials("fred", "flintstone");
        configuration.setURI("http://localhost:8080");
        configuration.setConnectionPoolSize(200);
        configuration.setEncryptionLevel("REQUIRED");
        configuration.setTrustStrategy("TRUST_SIGNED_CERTIFICATES");
        configuration.setTrustCertFile("/tmp/cert");

        assertEquals("assert", configuration.getAutoIndex());
        assertEquals("dir", configuration.getDumpDir());
        assertEquals("filename", configuration.getDumpFilename());
        assertEquals("driver", configuration.getDriverClassName());
        assertEquals("ZnJlZDpmbGludHN0b25l", configuration.getCredentials().credentials().toString());
        assertEquals("http://localhost:8080", configuration.getURI());
        assertEquals(Integer.valueOf(200), configuration.getConnectionPoolSize());
        assertEquals("REQUIRED", configuration.getEncryptionLevel());
        assertEquals("TRUST_SIGNED_CERTIFICATES", configuration.getTrustStrategy());
        assertEquals("/tmp/cert", configuration.getTrustCertFile());
    }

    @Test
    public void shouldConfigureCredentialsFromURI() {
        Configuration configuration = new Configuration();
        configuration.setURI("http://fred:flintstone@localhost:8080");
        assertEquals("ZnJlZDpmbGludHN0b25l", configuration.getCredentials().credentials().toString());
    }

    @Test
    public void shouldConfigureFromSimplePropertiesFile() {
        Configuration configuration = new Configuration(new ClasspathConfigurationSource("ogm-simple.properties"));

        assertEquals("none", configuration.getAutoIndex());
        assertEquals("org.neo4j.ogm.drivers.http.driver.HttpDriver", configuration.getDriverClassName());
        assertEquals("bmVvNGo6cGFzc3dvcmQ=", configuration.getCredentials().credentials().toString());
        assertEquals("http://localhost:7474", configuration.getURI());
    }

    @Test
    public void shouldConfigureFromNameSpacePropertiesFile() {

        Configuration configuration = new Configuration(new ClasspathConfigurationSource("ogm-namespace.properties"));

        assertEquals("dump", configuration.getAutoIndex());
        assertEquals("hello", configuration.getDumpDir());
        assertEquals("generated-indexes2.cql", configuration.getDumpFilename());
        assertEquals("org.neo4j.ogm.drivers.http.driver.HttpDriver", configuration.getDriverClassName());
        assertEquals("bmVvNGo6cGFzc3dvcmQ=", configuration.getCredentials().credentials().toString());
        assertEquals("http://localhost:7474", configuration.getURI());
        assertEquals(Integer.valueOf(100), configuration.getConnectionPoolSize());
        assertEquals("NONE", configuration.getEncryptionLevel());
        assertEquals("TRUST_ON_FIRST_USE", configuration.getTrustStrategy());
        assertEquals("/tmp/cert", configuration.getTrustCertFile());
    }

    @Test
    public void shouldConfigureFromSpringBootPropertiesFile() {

        Configuration configuration = new Configuration(new ClasspathConfigurationSource("application.properties"));

        assertEquals("none", configuration.getAutoIndex());
        assertEquals("org.neo4j.ogm.drivers.http.driver.HttpDriver", configuration.getDriverClassName());
        assertEquals("bmVvNGo6cGFzc3dvcmQ=", configuration.getCredentials().credentials().toString());
        assertEquals("http://localhost:7474", configuration.getURI());
    }
}
