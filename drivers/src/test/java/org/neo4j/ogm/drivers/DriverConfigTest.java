package org.neo4j.ogm.drivers;

import org.junit.Test;
import org.neo4j.ogm.api.config.Configuration;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;

import static org.junit.Assert.assertEquals;

/**
 * @author vince
 */

public class DriverConfigTest {

    @Test
    public void shouldLoadConfigFromPropertiesFile() {

        Configuration driverConfig = new Configuration("http.driver.properties");

        assertEquals("http://localhost:7474", driverConfig.getConfig("server"));
        assertEquals("neo4j", driverConfig.getConfig("username"));
        assertEquals("password", driverConfig.getConfig("password"));
    }

    @Test
    public void shouldAutoConfigureDriver() {

        Driver driver = new HttpDriver();

        assertEquals("http://localhost:7474", driver.getConfig("server"));
        assertEquals("neo4j", driver.getConfig("username"));
        assertEquals("password", driver.getConfig("password"));
    }

}
