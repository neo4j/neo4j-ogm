package org.neo4j.ogm.drivers;

import static org.junit.Assert.*;

import org.junit.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.service.Components;

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

    @Test
    public void shouldUseTemporaryEphemeralFileStoreForEmbeddedDriverIfNoURISpecified() {

        Configuration configuration = new Configuration();
        DriverConfiguration driverConfiguration = new DriverConfiguration(configuration);
        driverConfiguration.setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
        Components.configure(configuration);
        EmbeddedDriver driver = new EmbeddedDriver(driverConfiguration);

        assertNotNull(driverConfiguration.getURI());
        assertTrue(driverConfiguration.getURI().contains("/neo4j.db"));

        driver.close();

    }

}
