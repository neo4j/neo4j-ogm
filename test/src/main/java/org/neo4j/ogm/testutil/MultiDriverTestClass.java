package org.neo4j.ogm.testutil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.service.Components;

/**
 * @author vince
 */
public class MultiDriverTestClass {

    private static TestServer testServer;

    @BeforeClass
    public static void setupMultiDriverTestEnvironment() {
        if (Components.driver() instanceof HttpDriver) {
            if (Components.neo4jVersion() < 2.2) {
                testServer = new TestServer();
            } else {
                testServer = new AuthenticatingTestServer();
            }
        }
        System.out.println("Driver URI: " + Components.driver().getConfiguration().getURI());
    }

    @AfterClass
    public static void tearDownMultiDriverTestEnvironment() {
        if (testServer != null) {
            testServer.shutdown();
            testServer = null;
        } else {
            //((EmbeddedDriver) C)
        }
    }

    public static GraphDatabaseService getGraphDatabaseService() {
        if (testServer != null) {
            return testServer.getGraphDatabaseService();
        }
        return ((EmbeddedDriver) Components.driver()).getGraphDatabaseService();
    }
}
