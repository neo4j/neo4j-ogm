package org.neo4j.ogm.drivers.http;

import org.junit.Test;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.response.Response;
import org.neo4j.ogm.api.service.DriverService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author vince
 */
public class HttpDriverTest {

    private Driver driver = DriverService.lookup("http-test");

    @Test
    public void shouldGetGraphModelResponse() {

        Response<Graph> response = driver.requestHandler().execute(new DefaultGraphModelRequest("CREATE p=(n:ITEM {name:'item 1'})-[r:LINK {weight:4}]->(m:ITEM {sizes: [1,5,11], colours: ['red', 'green', 'blue']}) RETURN p"));

        Graph model;

        while ((model = response.next()) != null) {
            assertNotNull(model);
            assertEquals(2, model.getNodes().size());
            assertEquals(1, model.getRelationships().size());
        }
        response.close();
    }


}
