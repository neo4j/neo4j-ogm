package org.neo4j.ogm.testutil;

import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.embedded.EmbeddedDriver;

/**
 * @author vince
 */
public abstract class TestDriverFactory {

    private TestDriverFactory()  {
    }

    public static Driver driver(String strategy) {

        if (strategy.equals("http")) {
            return new TestServer().driver();
        }

        if (strategy.equals("embedded")) {
            return new EmbeddedDriver();
        }

        throw new RuntimeException("Not implemented: " + strategy);
    }
}
