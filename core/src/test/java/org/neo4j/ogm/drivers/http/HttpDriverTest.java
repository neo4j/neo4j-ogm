package org.neo4j.ogm.drivers.http;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.ogm.drivers.AbstractDriverTest;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.testutil.TestServer;

/**
 * @author vince
 */
public class HttpDriverTest extends AbstractDriverTest {

    private static TestServer server;

    @BeforeClass
    public static void configure() {
        Components.configure("ogm-http.properties");

        server = new TestServer();
    }

    @AfterClass
    public static void reset() {
        Components.autoConfigure();
        if (server != null) {
            server.shutdown();
        }
    }

    public void setUp() {
        assert Components.driver() instanceof HttpDriver;
    }

}
