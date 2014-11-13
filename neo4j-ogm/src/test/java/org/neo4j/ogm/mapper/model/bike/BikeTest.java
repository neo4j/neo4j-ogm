package org.neo4j.ogm.mapper.model.bike;

import org.junit.Test;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.mapper.domain.bike.Wheel;
import org.neo4j.ogm.session.DefaultSessionImpl;
import org.neo4j.ogm.session.SessionFactory;

import java.util.Collection;

import static org.junit.Assert.*;

public class BikeTest {

    @Test
    public void testDeserialiseBikeModel() throws Exception {

        BikeRequest bikeRequest = new BikeRequest();

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.mapper.domain.bike");
        DefaultSessionImpl session = ((DefaultSessionImpl) sessionFactory.openSession(null));
        session.setRequestHandler(bikeRequest);

        long now = -System.currentTimeMillis();
        Collection<Bike> bikes = session.loadAll(Bike.class);
        System.out.println("deserialised in " + (now + System.currentTimeMillis()) + " milliseconds");

        assertFalse(bikes.isEmpty());
        Bike bike = bikes.iterator().next();

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
