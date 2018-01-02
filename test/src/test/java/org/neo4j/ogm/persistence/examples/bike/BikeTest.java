/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.persistence.examples.bike;

import static org.assertj.core.api.Assertions.*;

import java.util.Collection;

import org.junit.Test;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.domain.bike.Wheel;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.session.Neo4jSession;

/**
 * @author Vince Bickers
 */
public class BikeTest {

    private static MetaData metadata = new MetaData("org.neo4j.ogm.domain.bike");
    private static Neo4jSession session = new Neo4jSession(metadata, new BikeRequest());

    @Test
    public void testDeserialiseBikeModel() throws Exception {

        Collection<Bike> bikes = session.loadAll(Bike.class);

        assertThat(bikes.isEmpty()).isFalse();
        Bike bike = bikes.iterator().next();

        assertThat(bike).isNotNull();
        assertThat((long) bike.getId()).isEqualTo(15);
        assertThat(bike.getColours().length).isEqualTo(2);

        // check the frame
        assertThat((long) bike.getFrame().getId()).isEqualTo(18);
        assertThat((int) bike.getFrame().getSize()).isEqualTo(27);

        // check the saddle
        assertThat((long) bike.getSaddle().getId()).isEqualTo(19);
        assertThat(bike.getSaddle().getPrice()).isCloseTo(42.99, within(0.00));
        assertThat(bike.getSaddle().getMaterial()).isEqualTo("plastic");

        // check the wheels
        assertThat(bike.getWheels()).hasSize(2);
        for (Wheel wheel : bike.getWheels()) {
            if (wheel.getId().equals(16L)) {
                assertThat((int) wheel.getSpokes()).isEqualTo(3);
            }
            if (wheel.getId().equals(17L)) {
                assertThat((int) wheel.getSpokes()).isEqualTo(5);
            }
        }
    }

    @Test
    public void testReloadExistingDomain() {

        Collection<Bike> bikes = session.loadAll(Bike.class);
        Collection<Bike> theSameBikes = session.loadAll(Bike.class);

        assertThat(theSameBikes.size()).isEqualTo(bikes.size());
    }
}
