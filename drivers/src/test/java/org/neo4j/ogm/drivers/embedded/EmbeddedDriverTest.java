package org.neo4j.ogm.drivers.embedded;

import org.junit.Test;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.service.DriverService;

/**
 * @author vince
 */
public class EmbeddedDriverTest {

    private Driver driver = DriverService.lookup("embedded");

    @Test
    public void shouldGetGraphModelResponse() {

//        Response<Graph> response = driver.requestHandler().execute(new DefaultGraphModelRequest("CREATE p=(n:ITEM {name:'item 1'})-[r:LINK {weight:4}]->(m:ITEM {sizes: [1,5,11], colours: ['red', 'green', 'blue']}) RETURN p"));
//
//        Graph model = response.next();
//
//        assertNotNull(model);
//        assertEquals(2, model.getNodes().size());
//        assertEquals(1, model.getRelationships().size());
//
//        response.close();
    }

}
