package org.neo4j.ogm.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graphaware.graphmodel.neo4j.EdgeModel;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertNotNull;

public class TestGraphModelToObjectMapper {

    @Test
    public void testAcceptGraphModelAndDoNothing() {

        GraphModelToObjectMapper mapper = new GraphModelToObjectMapper<Void, GraphModel>() {

            @Override
            public Void mapToObject(GraphModel graphModel) {
                assertNotNull(graphModel);
                return null;
            }
        };

        assertNull(mapper.mapToObject(new GraphModel()));
    }

}
