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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author vince
 */
public class ConfigurationTest {

    @Test
    public void shouldConfigureProgrammatically() {

        Configuration configuration = new Configuration();

        configuration.compilerConfiguration().setCompilerClassName("compiler");
        configuration.driverConfiguration().setDriverClassName("driver");
        configuration.driverConfiguration().setCredentials("fred", "flintstone");
        configuration.driverConfiguration().setURI("http://localhost:8080");

        assertEquals("compiler", configuration.compilerConfiguration().getCompilerClassName());
        assertEquals("driver", configuration.driverConfiguration().getDriverClassName());
        assertEquals("ZnJlZDpmbGludHN0b25l", configuration.driverConfiguration().getCredentials().credentials().toString());
        assertEquals("http://localhost:8080", configuration.driverConfiguration().getURI());


    }

    @Test
    public void shouldConfigureCredentialsFromURI() {
        Configuration configuration = new Configuration();
        configuration.driverConfiguration().setURI("http://fred:flintstone@localhost:8080");
        assertEquals("ZnJlZDpmbGludHN0b25l", configuration.driverConfiguration().getCredentials().credentials().toString());
    }

    @Test
    public void shouldProvideDefaultCompilerImplementationIfNoneSpecified() {
        Configuration configuration = new Configuration();
        assertEquals("org.neo4j.ogm.compiler.MultiStatementCypherCompiler", configuration.compilerConfiguration().getCompilerClassName());
    }

    @Test
    public void shouldConfigureFromPropertiesFile() {

        Configuration configuration = new Configuration("ogm.properties");

        assertEquals("org.neo4j.ogm.compiler.MultiStatementCypherCompiler", configuration.compilerConfiguration().getCompilerClassName());
        assertEquals("org.neo4j.ogm.drivers.http.driver.HttpDriver", configuration.driverConfiguration().getDriverClassName());
        assertEquals("bmVvNGo6cGFzc3dvcmQ=", configuration.driverConfiguration().getCredentials().credentials().toString());
        assertEquals("http://neo4j:password@localhost:7474", configuration.driverConfiguration().getURI());

    }
}
