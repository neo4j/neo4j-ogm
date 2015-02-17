/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.unit.mapper.model.bike;

import org.junit.Test;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.domain.bike.Wheel;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;

import java.util.Collection;

import static org.junit.Assert.*;

public class BikeTest {

    @Test
    public void testDeserialiseBikeModel() throws Exception {

        BikeRequest bikeRequest = new BikeRequest();

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.bike");
        Neo4jSession session = ((Neo4jSession) sessionFactory.openSession("dummy-url"));
        session.setRequest(bikeRequest);

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

    @Test
    public void testReloadExistingDomain() {

        BikeRequest bikeRequest = new BikeRequest();

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.bike");
        Neo4jSession session = ((Neo4jSession) sessionFactory.openSession("dummy-url"));
        session.setRequest(bikeRequest);

        Collection<Bike> bikes = session.loadAll(Bike.class);
        Collection<Bike> theSameBikes = session.loadAll(Bike.class);

        assertEquals(bikes.size(), theSameBikes.size());

    }

}
