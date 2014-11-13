package org.neo4j.ogm.performance;

import org.junit.Test;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.mapper.model.bike.BikeRequest;
import org.neo4j.ogm.session.DefaultSessionImpl;
import org.neo4j.ogm.session.SessionFactory;

import static org.junit.Assert.assertTrue;

public class DeserialisationPerformanceTest {

    @Test
    public void testAverageDeserialisationSpeed() throws Exception {

        int count = 1000;
        int target =3000;          // maximum permitted time (milliseconds) to load <count> entities;

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.mapper.domain.bike");
        DefaultSessionImpl session = ((DefaultSessionImpl) sessionFactory.openSession(null));
        session.setRequestHandler(new BikeRequest());


        long elapsed = -System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            session.loadAll(Bike.class);
        }
        elapsed += System.currentTimeMillis();

        System.out.println("Deserialised Bike " + count + " times in " + elapsed + " milliseconds");

        assertTrue(elapsed < target);
    }


}