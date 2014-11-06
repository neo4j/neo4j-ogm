package org.neo4j.ogm.mapper.model.bike.performance;

import org.junit.Test;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.mapper.model.bike.BikeRequest;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import static org.junit.Assert.assertTrue;

public class PerformanceTest {

    @Test
    public void testAverageDeserialisationSpeed() throws Exception {

        int count = 1000;
        int target =3000;          // maximum permitted time (milliseconds) to load <count> entities;

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.mapper.domain.bike");
        Session session = sessionFactory.openSession(null);
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