package org.neo4j.ogm.drivers.embedded;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.AbstractDriverTestSuite;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.service.Components;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * @author vince
 */
public class EmbeddedDriverTest extends AbstractDriverTestSuite {


    private static final GraphDatabaseService graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabase();
    private static final Driver driver = new EmbeddedDriver(graphDatabaseService);

    @BeforeClass
    public static void configure() {
        Components.configure("ogm-embedded.properties");
    }

    @AfterClass
    public static void reset() {
        Components.autoConfigure();
    }

    public void setUp() {
        Components.setDriver(driver);
        assert Components.driver() instanceof EmbeddedDriver;
    }

}
