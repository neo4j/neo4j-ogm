/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.persistence.examples.restaurant.spatial;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.restaurant.Diner;
import org.neo4j.ogm.domain.restaurant.Location;
import org.neo4j.ogm.domain.restaurant.Restaurant;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DinerIntegrationTest extends MultiDriverTestClass {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory("org.neo4j.ogm.domain.restaurant").openSession();
    }

    @After
    public void teardown() {
        session.purgeDatabase();
    }

    @Test
    public void saveAndRetrieveRestaurantWithLocation() {

        Diner diner = new Diner("Jasper", "Blues", new Location(37.61649, -122.38681));

        session.save(diner);
        session.clear();

        Collection<Diner> results = session.loadAll(Diner.class, new Filter("firstName", "Jasper"));
        assertEquals(1, results.size());
        Diner result = results.iterator().next();
        assertNotNull(result.getLocation());
        assertEquals(37.61649, result.getLocation().getLatitude(), 0);
        assertEquals(-122.38681, result.getLocation().getLongitude(), 0);
    }

}
