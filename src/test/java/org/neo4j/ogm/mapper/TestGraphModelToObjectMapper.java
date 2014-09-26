package org.neo4j.ogm.mapper;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertNotNull;

public class TestGraphModelToObjectMapper {

    @Test
    public void testAcceptGraphModelAndDoNothing() {

        GraphModelToObjectMapper mapper = new GraphModelToObjectMapper<GraphModel>() {

            @Override
            public Void mapToObject(GraphModel graphModel) {
                assertNotNull(graphModel);
                return null;
            }
        };

        assertNull(mapper.mapToObject(new GraphModel()));
    }

}
