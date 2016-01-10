package org.neo4j.ogm.testutil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.service.Components;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * @author vince
 */
public class MultiDriverTestClass {

    private static TestServer testServer;
    private static GraphDatabaseService impermanentDb;

    @BeforeClass
    public static void setupMultiDriverTestEnvironment() {

        if (Components.driver() instanceof HttpDriver) {
            if (Components.neo4jVersion() < 2.2) {
                testServer = new TestServer.Builder()
                        .enableAuthentication(false)
                        .transactionTimeoutSeconds(2)
                        .build();
            } else {
                testServer = new TestServer.Builder()
                        .enableAuthentication(true)
                        .transactionTimeoutSeconds(2)
                        .build();
            }
        }
        else {
            impermanentDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
            Components.setDriver(new EmbeddedDriver(impermanentDb));
        }
    }

    @AfterClass
    public static void tearDownMultiDriverTestEnvironment() {
        if (testServer != null) {
            testServer.shutdown();
            testServer = null;
        }
        if (impermanentDb != null) {
            impermanentDb.shutdown();
            impermanentDb = null;
        }
    }

    public static GraphDatabaseService getGraphDatabaseService() {
        if (testServer != null) {
            return testServer.getGraphDatabaseService();
        }
        return impermanentDb;
    }
}
