package org.neo4j.ogm.testutil;

import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.spi.DriverService;

/**
 * @author vince
 */
public abstract class TestDriverFactory {

    private TestDriverFactory()  {
    }

    public static Driver driver(String strategy) {
        return DriverService.lookup(strategy);
    }
}
