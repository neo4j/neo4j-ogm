package org.neo4j.ogm.mapper;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.graphaware.graphmodel.neo4j.GraphModel;
import org.junit.Test;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.mapper.domain.bike.Wheel;
import org.neo4j.ogm.mapper.model.BikeModel;
import org.neo4j.ogm.metadata.ClassDictionary;
import org.neo4j.ogm.metadata.DefaultConstructorObjectFactory;
import org.neo4j.ogm.metadata.MatchingFieldAndPropertyPersitentFieldDictionary;
import org.neo4j.ogm.strategy.SetterEntityAccessFactory;
import org.neo4j.ogm.strategy.simple.ObjectGraphMapper;

public class BikeTest {

    private static GraphModelToObjectMapper<GraphModel> instantiateMapper() {
        DefaultConstructorObjectFactory objectCreationStrategy = new DefaultConstructorObjectFactory(new ClassDictionary() {
            @Override
            public String determineBaseClass(List<String> taxa) {
                return "org.neo4j.ogm.mapper.domain.bike." + taxa.get(0);
            }

            @Override
            public List<String> getFQNs(String simpleName) {
                return Collections.singletonList("org.neo4j.ogm.mapper.domain.bike." + simpleName);
            }
        });
        return new ObjectGraphMapper(Bike.class, objectCreationStrategy,
                new SetterEntityAccessFactory(), new MatchingFieldAndPropertyPersitentFieldDictionary());
    }

    @Test
    public void testDeserialiseBikeModel() throws Exception {

        GraphModel graphModel = BikeModel.load();

        long now = -System.currentTimeMillis();
        Bike bike = (Bike) instantiateMapper().mapToObject(graphModel);
        System.out.println("deserialised in " + (now + System.currentTimeMillis()) + " milliseconds");

        assertNotNull(bike);
        assertEquals(15, (long) bike.getId());
        assertEquals(2, bike.getColours().length);

        // check the frame
        assertEquals(18, (long) bike.getFrame().getId());
        assertEquals(27, (int) bike.getFrame().getSize());

        // check the saddle
        assertEquals(19, (long) bike.getSaddle().getId());
        assertEquals(42.99, bike.getSaddle().getPrice(), 0.00);
        assertEquals("plastic", bike.getSaddle().getMaterial());

        // check the wheels
        assertEquals(2, bike.getWheels().size());
        for (Wheel wheel : bike.getWheels()) {
            if (wheel.getId().equals(16L)) {
                assertEquals(3, (int) wheel.getSpokes());
            }
            if (wheel.getId().equals(17L)) {
                assertEquals(5, (int) wheel.getSpokes());
            }
        }
    }

}
