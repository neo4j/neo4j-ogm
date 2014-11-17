package org.neo4j.ogm.integration;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 *  Temporary playground for the full cycle.
 */
public class EndToEndTest extends IntegrationTest {

    @Before
    public void init() throws IOException {
        super.setUp();
        session = new SessionFactory("org.neo4j.ogm.domain.bike").openSession("http://localhost:" + neoPort);
    }

    @Test
    public void canSaveNewObjectToDatabase() {

        Bike bike = new Bike();

        assertEquals(null, bike.getId());

        session.purge();
        session.save(bike);

        // 1st object always has id 0
        assertEquals(new Long(0), bike.getId());

    }
}
