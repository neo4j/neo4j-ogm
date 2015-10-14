package org.neo4j.ogm.unit.drivers.embedded;

import org.junit.Test;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.response.Response;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.spi.DriverService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author vince
 */
public class EmbeddedDriverTest {

    private Driver driver = DriverService.lookup("embedded");

    @Test
    public void shouldGetGraphModelResponse() {

        Response<Graph> response = driver.requestHandler().execute(new DefaultGraphModelRequest("CREATE p=(n:ITEM {name:'item 1'})-[r:LINK {weight:4}]->(m:ITEM {sizes: [1,5,11], colours: ['red', 'green', 'blue']}) RETURN p"));

        Graph model = response.next();

        assertNotNull(model);
        assertEquals(2, model.getNodes().size());
        assertEquals(1, model.getRelationships().size());

        response.close();
    }

}
