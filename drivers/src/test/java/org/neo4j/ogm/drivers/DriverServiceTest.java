package org.neo4j.ogm.drivers;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.spi.DriverService;
import org.neo4j.ogm.spi.ServiceNotFoundException;

import static org.junit.Assert.assertNotNull;

/**
 * @author vince
 */
public class DriverServiceTest {

    @Test
    public void shouldLoadHttpDriver() {
        Driver driver = DriverService.load("org.neo4j.ogm.driver.http.driver.HttpDriver");
        assertNotNull(driver);
    }

    @Test
    public void shouldLookupHttpDriver() {
        Driver driver = DriverService.lookup("http");
        assertNotNull(driver);
    }

    @Test
    public void shouldLoadEmbeddedDriver() {
        Driver driver = DriverService.load("org.neo4j.ogm.driver.embedded.driver.EmbeddedDriver");
        assertNotNull(driver);
    }

    @Test
    public void shouldLookupEmbeddedDriver() {
        Driver driver = DriverService.lookup("embedded");
        assertNotNull(driver);
    }

    @Test
    @Ignore // until we can switch to 3.0 in memory
    public void loadLoadBoltDriver() {
        Driver driver = DriverService.load("org.neo4j.ogm.driver.bolt.driver.BoltDriver");
        assertNotNull(driver);
    }

    @Test
    @Ignore // until we can switch to 3.0 in memory
    public void shouldLookupBoltDriver() {
        Driver driver = DriverService.lookup("bolt");
        assertNotNull(driver);
    }

    @Test(expected = ServiceNotFoundException.class)
    public void shouldFailForMissingOrUnregisteredDriver() {
        Driver driver = DriverService.load("Unregistered");
        assertNotNull(driver);
    }

}
