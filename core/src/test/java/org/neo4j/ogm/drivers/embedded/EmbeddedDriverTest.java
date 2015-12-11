package org.neo4j.ogm.drivers.embedded;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.AbstractDriverTest;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.service.Components;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * @author vince
 */
public class EmbeddedDriverTest extends AbstractDriverTest {

    private static final GraphDatabaseService graphDatabaseService = new TestGraphDatabaseFactory().newImpermanentDatabase();
    private static final Driver driver = new EmbeddedDriver(graphDatabaseService);

    public void setUp() {
        Components.setDriver(driver);
        assert Components.driver() instanceof EmbeddedDriver;
    }


}
