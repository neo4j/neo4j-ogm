/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    private static Neo4jSession session = new Neo4jSession(metadata, true, new BikeRequest());

    @Test
    public void testDeserialiseBikeModel() {

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
