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
