package org.neo4j.ogm.drivers;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.service.DriverService;

import static org.junit.Assert.assertNotNull;

/**
 * @author vince
 */
public class DriverServiceTest {

    private DriverConfiguration driverConfiguration = new DriverConfiguration();

    @Test
    public void shouldLoadHttpDriver() {

        driverConfiguration.setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver");
        driverConfiguration.setURI("http://neo4j:password@localhost:7474");
        Driver driver = DriverService.load(driverConfiguration);
        assertNotNull(driver);
    }

    @Test
    public void shouldLoadEmbeddedDriver() {
        driverConfiguration.setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
        driverConfiguration.setURI("file:///tmp/neo4j.db");
        Driver driver = DriverService.load(driverConfiguration);
        assertNotNull(driver);
    }

    @Test
    @Ignore // until we can switch to 3.0 in memory
    public void loadLoadBoltDriver() {
        driverConfiguration.setDriverClassName("org.neo4j.ogm.drivers.bolt.driver.BoltDriver");
        driverConfiguration.setURI("bolt://neo4j:password@localhost");
        Driver driver = DriverService.load(driverConfiguration);
        assertNotNull(driver);
    }

}
