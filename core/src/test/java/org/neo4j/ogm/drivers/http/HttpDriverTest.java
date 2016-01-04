package org.neo4j.ogm.drivers.http;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.neo4j.ogm.drivers.AbstractDriverTestSuite;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.testutil.TestServer;

/**
 * @author vince
 */
public class HttpDriverTest extends AbstractDriverTestSuite {

//    private TestServer server;

    @BeforeClass
    public static void configure() {
        Components.configure("ogm-http.properties");
        new TestServer();
    }

    @AfterClass
    public static void reset() {
        Components.autoConfigure();
    }

    @Before
    public void setUp() {
        assert Components.driver() instanceof HttpDriver;
//        if (server == null) {
//            server = new TestServer();
//        }
    }

}
