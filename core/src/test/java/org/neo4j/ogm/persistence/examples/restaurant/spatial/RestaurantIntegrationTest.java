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

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.DistanceComparison;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.FilterFunction;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.domain.restaurant.Location;
import org.neo4j.ogm.domain.restaurant.Restaurant;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RestaurantIntegrationTest extends MultiDriverTestClass {

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
    public void shouldSaveRestaurant() {
        Restaurant restaurant = new Restaurant("San Francisco International Airport (SFO)",
                new Location(37.61649, -122.38681), 94128);
        session.save(restaurant);
    }

    @Test
    public void shouldQueryByDistance() {
        Restaurant restaurant = new Restaurant("San Francisco International Airport (SFO)",
                new Location(37.61649, -122.38681), 94128);
        session.save(restaurant);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("distance", 1000);

        Restaurant found = session.queryForObject(Restaurant.class, "MATCH (r:Restaurant) " +
                        "WHERE distance(point(r),point({latitude:37.0, longitude:-118.0, crs: 'WGS-84'})) < {distance}*1000 RETURN r;",
                parameters);

        Assert.assertNotNull(found);
    }

    @Test
    public void shouldQueryByDistanceUsingFilter() {
        Restaurant restaurant = new Restaurant("San Francisco International Airport (SFO)",
                new Location(37.61649, -122.38681), 94128);
        session.save(restaurant);
        session.clear();

        Filter filter = new Filter(FilterFunction.DISTANCE, new DistanceComparison(37.61649, -122.38681, 1000 * 1000.0));
        filter.setComparisonOperator(ComparisonOperator.LESS_THAN);
        Collection<Restaurant> found = session.loadAll(Restaurant.class, filter);
        Assert.assertNotNull(found);
        assertTrue(found.size() >= 1);
    }

    @Test
    public void saveAndRetrieveRestaurantWithLocation() {

        Restaurant restaurant = new Restaurant("San Francisco International Airport (SFO)",
                new Location(37.61649, -122.38681), 94128);

        session.save(restaurant);
        session.clear();

        Collection<Restaurant> results = session.loadAll(Restaurant.class,
                new Filter("name", "San Francisco International Airport (SFO)"));
        assertEquals(1, results.size());
        Restaurant result = results.iterator().next();
        assertNotNull(result.getLocation());
        assertEquals(37.61649, result.getLocation().getLatitude(), 0);
        assertEquals(-122.38681, result.getLocation().getLongitude(), 0);
    }

}
