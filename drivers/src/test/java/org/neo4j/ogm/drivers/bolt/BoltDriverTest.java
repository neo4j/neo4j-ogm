package org.neo4j.ogm.drivers.bolt;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.service.DriverService;

/**
 * @author vince
 */
@Ignore
public class BoltDriverTest {

    private Driver driver = DriverService.lookup("bolt");

    @Test
    public void shouldUseGraphModelResponseAdapter() {

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
