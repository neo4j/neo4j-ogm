package org.neo4j.ogm.integration;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.domain.bike.Wheel;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;
import java.util.Arrays;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

/**
 *  Temporary playground for the full cycle.
 */
public class EndToEndTest extends IntegrationTest {

    @BeforeClass
    public static void init() throws IOException {
        setUp();
        session = new SessionFactory("org.neo4j.ogm.domain.bike").openSession("http://localhost:" + neoPort);
    }

    @Test
    public void canSaveNewObjectTreeToDatabase() {

        Wheel frontWheel = new Wheel();
        Wheel backWheel = new Wheel();
        Bike bike = new Bike();

        // TODO: can't persist the 1-side of an object relationship...
        //bike.setFrame(new Frame());
        //bike.setSaddle(new Saddle());
        bike.setWheels(Arrays.asList(frontWheel, backWheel));

        assertNull(frontWheel.getId());
        assertNull(backWheel.getId());
        assertNull(bike.getId());
        //assertNull(bike.getFrame().getId());
        //assertNull(bike.getSaddle().getId());

        session.purge();
        session.save(bike);

        assertNotNull(frontWheel.getId());
        assertNotNull(backWheel.getId());
        assertNotNull(bike.getId());
        //assertNotNull(bike.getFrame().getId());
        //assertNotNull(bike.getSaddle().getId());

    }
}
