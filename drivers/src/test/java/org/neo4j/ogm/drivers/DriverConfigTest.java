package org.neo4j.ogm.drivers;

import org.junit.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverConfiguration;

import static org.junit.Assert.assertEquals;

/**
 * @author vince
 */

public class DriverConfigTest {

    @Test
    public void shouldLoadHttpDriverConfigFromPropertiesFile() {
        DriverConfiguration driverConfig = new DriverConfiguration(new Configuration("http.driver.properties"));
        assertEquals("http://neo4j:password@localhost:7474", driverConfig.getURI());
    }

    @Test
    public void shouldLoadEmbeddedDriverConfigFromPropertiesFile() {
        DriverConfiguration driverConfig = new DriverConfiguration(new Configuration("embedded.driver.properties"));
        assertEquals("file:///tmp/neo4j.db", driverConfig.getURI());
    }

    @Test
    public void shouldLoadBoltDriverConfigFromPropertiesFile() {
        DriverConfiguration driverConfig = new DriverConfiguration(new Configuration("bolt.driver.properties"));
        assertEquals("bolt://neo4j:password@localhost", driverConfig.getURI());
    }
}
