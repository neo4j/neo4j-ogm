package org.neo4j.ogm.integration;

import org.junit.Test;
import org.neo4j.ogm.mapper.domain.bike.Bike;

/**
 *  Temporary playground for the full cycle.
 */
public class EndToEndTest extends IntegrationTest {

    @Test
    public void canSaveModelToEmptyDatabase() {
        Bike bike = new Bike();

        //save bike,...
    }
}
