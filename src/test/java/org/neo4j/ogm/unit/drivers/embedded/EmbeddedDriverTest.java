package org.neo4j.ogm.unit.drivers.embedded;

import org.junit.Test;
import org.neo4j.ogm.cypher.query.GraphModelRequest;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.session.response.Response;
import org.neo4j.ogm.session.response.model.GraphModel;
import org.neo4j.ogm.testutil.TestDriverFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author vince
 */
public class EmbeddedDriverTest {

    private Driver driver = TestDriverFactory.driver("embedded");

    @Test
    public void shouldGetGraphModelResponse() {

        Response<GraphModel> response = driver.requestHandler().execute(new GraphModelRequest("CREATE p=(n:ITEM {name:'item 1'})-[r:LINK {weight:4}]->(m:ITEM {sizes: [1,5,11], colours: ['red', 'green', 'blue']}) RETURN p"));

        GraphModel model = response.next();

        assertNotNull(model);
        assertEquals(2, model.getNodes().size());
        assertEquals(1, model.getRelationships().size());

        response.close();
    }

}
