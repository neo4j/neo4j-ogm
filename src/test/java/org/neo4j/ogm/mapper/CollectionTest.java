package org.neo4j.ogm.mapper;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.mapper.domain.collection.Child;
import org.neo4j.ogm.mapper.domain.collection.Parent;
import org.neo4j.ogm.mapper.model.CollectionModel;
import org.neo4j.ogm.strategy.simple.SimpleSetterMappingStrategy;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

public class CollectionTest {

    private CollectionModel request;

    @Before
    public void setUp() {
        request = new CollectionModel();
    }


    @Test
    public void testMultiplePathResponse() throws Exception {

        SimpleSetterMappingStrategy mapper = new SimpleSetterMappingStrategy(Parent.class, "org.neo4j.ogm.mapper.domain.collection");
        mapper.reset();

        GraphModel graphModel;
        Parent parent = new Parent();

        while ((graphModel = request.getResponse().next()) != null) {
            parent = (Parent) mapper.mapToObject(graphModel);
        }

        assertEquals(2, parent.getChildren().size());

        Child child1 = parent.getChildren().get(0);
        Child child2 = parent.getChildren().get(1);

        assertNotSame(child1, child2);

        if (child1.getId() == 2L) {
            assertEquals("Bill", child1.getName());
        } else {
            assertEquals("Bill", child2.getName());
        }

        if (child1.getId() == 3L) {
            assertEquals("Mary", child1.getName());
        } else {
            assertEquals("Mary", child2.getName());
        }

    }
}
