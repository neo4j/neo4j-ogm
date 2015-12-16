package org.neo4j.ogm.testutil;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.config.DriverConfiguration;
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
                testServer = new TestServer();
            } else {
                testServer = new AuthenticatingTestServer();
            }
        }
        else {
            impermanentDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
            DriverConfiguration driverConfiguration = new DriverConfiguration();
            driverConfiguration.setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
            driverConfiguration.setURI("file:///tmp/neo4j.db"); //this should not matter with an impermanent db
            EmbeddedDriver embeddedDriver = new EmbeddedDriver(impermanentDb);
            Components.setDriver(embeddedDriver);
            Components.driver().configure(driverConfiguration);
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
        }
    }

    public static GraphDatabaseService getGraphDatabaseService() {
        if (testServer != null) {
            return testServer.getGraphDatabaseService();
        }
        return ((EmbeddedDriver) Components.driver()).getGraphDatabaseService();
    }
}
