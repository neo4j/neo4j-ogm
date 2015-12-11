package org.neo4j.ogm.drivers.http;

import org.neo4j.ogm.drivers.AbstractDriverTest;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.testutil.TestServer;

/**
 * @author vince
 */
public class HttpDriverTest extends AbstractDriverTest {

    private static final TestServer server = new TestServer();

    public void setUp() {
        assert Components.driver() instanceof HttpDriver;
    }

}
