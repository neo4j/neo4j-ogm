package org.neo4j.ogm.integration;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;

/**
 *  Temporary playground for the full cycle.
 */
public class EndToEndTest extends IntegrationTest {

    @Before
    public void importData() throws IOException {
        super.setUp();
        session = new SessionFactory("org.neo4j.ogm.mapper.domain.bike").openSession("http://localhost:" + neoPort);
    }

    @Test
    public void canSaveModelToEmptyDatabase() {
        Bike bike = new Bike();

        session.save(bike);
        //save bike,...
    }
}
