package org.neo4j.ogm.testutil;

import org.neo4j.ogm.session.Driver;
import org.neo4j.ogm.driver.bolt.driver.BoltDriver;
import org.neo4j.ogm.driver.embedded.driver.EmbeddedDriver;

/**
 * @author vince
 */
public abstract class TestDriverFactory {

    // shared static instance so we don't run into store lock problems during tests.
    private static final Driver EMBEDDED_DRIVER = new EmbeddedDriver();

    private TestDriverFactory()  {
    }

    public static Driver driver(String strategy) {

        if (strategy.equals("http")) {
            return new TestServer().driver();
        }

        if (strategy.equals("embedded")) {
            return EMBEDDED_DRIVER;
        }

        if (strategy.equals("bolt")) {
            return new BoltDriver();
        }
        throw new RuntimeException("Not implemented: " + strategy);
    }
}
