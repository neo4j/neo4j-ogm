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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.neo4j.ogm.autoindex.AutoIndexMode;

/**
 * @author vince
 */
public class ConfigurationTest {

    @Test
    public void shouldConfigureProgrammatically() {

        Configuration configuration = new Configuration();

        configuration.compilerConfiguration().setCompilerClassName("compiler");
        configuration.autoIndexConfiguration().setAutoIndex("assert");
        configuration.autoIndexConfiguration().setDumpDir("dir");
        configuration.autoIndexConfiguration().setDumpFilename("filename");
        configuration.driverConfiguration().setDriverClassName("driver");
        configuration.driverConfiguration().setCredentials("fred", "flintstone");
        configuration.driverConfiguration().setURI("http://localhost:8080");
        configuration.driverConfiguration().setConnectionPoolSize(200);
        configuration.driverConfiguration().setEncryptionLevel("REQUIRED");
        configuration.driverConfiguration().setTrustStrategy("TRUST_SIGNED_CERTIFICATES");
        configuration.driverConfiguration().setTrustCertFile("/tmp/cert");

        assertEquals("compiler", configuration.compilerConfiguration().getCompilerClassName());
        assertEquals(AutoIndexMode.ASSERT, configuration.autoIndexConfiguration().getAutoIndex());
        assertEquals("dir", configuration.autoIndexConfiguration().getDumpDir());
        assertEquals("filename", configuration.autoIndexConfiguration().getDumpFilename());
        assertEquals("driver", configuration.driverConfiguration().getDriverClassName());
        assertEquals("ZnJlZDpmbGludHN0b25l", configuration.driverConfiguration().getCredentials().credentials().toString());
        assertEquals("http://localhost:8080", configuration.driverConfiguration().getURI());
        assertEquals(Integer.valueOf(200), configuration.driverConfiguration().getConnectionPoolSize());
        assertEquals("REQUIRED", configuration.driverConfiguration().getEncryptionLevel());
        assertEquals("TRUST_SIGNED_CERTIFICATES", configuration.driverConfiguration().getTrustStrategy());
        assertEquals("/tmp/cert", configuration.driverConfiguration().getTrustCertFile());
    }

    @Test
    public void shouldConfigureCredentialsFromURI() {
        Configuration configuration = new Configuration();
        configuration.driverConfiguration().setURI("http://fred:flintstone@localhost:8080");
        assertEquals("ZnJlZDpmbGludHN0b25l", configuration.driverConfiguration().getCredentials().credentials().toString());
    }

    @Test
    public void shouldConfigureFromSimplePropertiesFile() {
        Configuration configuration = new Configuration("ogm-simple.properties");

        assertEquals("org.neo4j.ogm.compiler.MultiStatementCypherCompiler", configuration.compilerConfiguration().getCompilerClassName());
        assertEquals(AutoIndexMode.NONE, configuration.autoIndexConfiguration().getAutoIndex());
        assertEquals("org.neo4j.ogm.drivers.http.driver.HttpDriver", configuration.driverConfiguration().getDriverClassName());
        assertEquals("bmVvNGo6cGFzc3dvcmQ=", configuration.driverConfiguration().getCredentials().credentials().toString());
        assertEquals("http://neo4j:password@localhost:7474", configuration.driverConfiguration().getURI());
    }

    @Test
    public void shouldConfigureFromNameSpacePropertiesFile() {

        Configuration configuration = new Configuration("ogm-namespace.properties");

        assertEquals("org.neo4j.ogm.compiler.MultiStatementCypherCompiler", configuration.compilerConfiguration().getCompilerClassName());
        assertEquals(AutoIndexMode.DUMP, configuration.autoIndexConfiguration().getAutoIndex());
        assertEquals("hello", configuration.autoIndexConfiguration().getDumpDir());
        assertEquals("generated-indexes2.cql", configuration.autoIndexConfiguration().getDumpFilename());
        assertEquals("org.neo4j.ogm.drivers.http.driver.HttpDriver", configuration.driverConfiguration().getDriverClassName());
        assertEquals("bmVvNGo6cGFzc3dvcmQ=", configuration.driverConfiguration().getCredentials().credentials().toString());
        assertEquals("http://neo4j:password@localhost:7474", configuration.driverConfiguration().getURI());
        assertEquals(Integer.valueOf(100), configuration.driverConfiguration().getConnectionPoolSize());
        assertEquals("NONE", configuration.driverConfiguration().getEncryptionLevel());
        assertEquals("TRUST_ON_FIRST_USE", configuration.driverConfiguration().getTrustStrategy());
        assertEquals("/tmp/cert", configuration.driverConfiguration().getTrustCertFile());

    }

    @Test
    public void shouldConfigureFromSpringBootPropertiesFile() {

        Configuration configuration = new Configuration("application.properties");

        assertEquals("org.neo4j.ogm.compiler.MultiStatementCypherCompiler", configuration.compilerConfiguration().getCompilerClassName());
        assertEquals(AutoIndexMode.NONE, configuration.autoIndexConfiguration().getAutoIndex());
        assertEquals("org.neo4j.ogm.drivers.http.driver.HttpDriver", configuration.driverConfiguration().getDriverClassName());
        assertEquals("bmVvNGo6cGFzc3dvcmQ=", configuration.driverConfiguration().getCredentials().credentials().toString());
        assertEquals("http://neo4j:password@localhost:7474", configuration.driverConfiguration().getURI());

    }

}
