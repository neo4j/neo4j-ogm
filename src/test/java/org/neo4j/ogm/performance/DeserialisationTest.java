package org.neo4j.ogm.performance;

import org.junit.Test;
import org.neo4j.ogm.TestCypherQuery;
import org.neo4j.ogm.mapper.cypher.CypherQuery;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.mapper.model.BikeModel;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeserialisationTest {

    private final int count = 1000;
    private final int target =3000;          // maximum permitted time (milliseconds) to load <count> entities;
    private final CypherQuery query = new TestCypherQuery(BikeModel.jsonModel(), count);
    private final SessionFactory sessionFactory = new SessionFactory(query, "org.neo4j.ogm.mapper.domain.bike");

    @Test
    public void testAverageDeserialisationSpeed() throws Exception {

        Session session = sessionFactory.openSession();

        long elapsed = -System.currentTimeMillis();
        Collection<Bike> bikes = session.load(Bike.class);
        elapsed += System.currentTimeMillis();

        System.out.println("Deserialised Bike " + count + " times in " + elapsed + " milliseconds");

        assertEquals(1000, bikes.size());
        assertTrue(elapsed < target);
    }


}