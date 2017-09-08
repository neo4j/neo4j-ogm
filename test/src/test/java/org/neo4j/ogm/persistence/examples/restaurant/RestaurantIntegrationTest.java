/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.persistence.examples.restaurant;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.*;

import org.junit.*;
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.function.DistanceComparison;
import org.neo4j.ogm.cypher.function.DistanceFromPoint;
import org.neo4j.ogm.domain.restaurant.Branch;
import org.neo4j.ogm.domain.restaurant.Franchise;
import org.neo4j.ogm.domain.restaurant.Location;
import org.neo4j.ogm.domain.restaurant.Restaurant;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

public class RestaurantIntegrationTest extends MultiDriverTestClass {

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.restaurant");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @After
    public void teardown() {
        session.purgeDatabase();
    }

    @Test
    public void shouldSaveRestaurantWithCompositeLocationConverter() {

        Restaurant restaurant = new Restaurant("San Francisco International Airport (SFO)",
                new Location(37.61649, -122.38681), 94128);
        session.save(restaurant);

        GraphTestUtils.assertSameGraph(getGraphDatabaseService(),
                "CREATE (n:`Restaurant` {name: 'San Francisco International Airport (SFO)', latitude: 37.61649, longitude: -122.38681, zip: 94128, score: 0.0, halal: false})");
    }

    @Test
    public void shouldSaveBranchWitlCompositeLocationConverter() throws Exception {
        Franchise franchise = new Franchise();
        Restaurant restaurant = new Restaurant();
        Branch branch = new Branch(new Location(37.61649, -122.38681), franchise, restaurant);

        session.save(branch);
        session.clear();

        Branch loaded = session.load(Branch.class, branch.getId());
        assertThat(loaded.getLocation().getLatitude()).isCloseTo(37.61649, within(0.00001));
        assertThat(loaded.getLocation().getLongitude()).isCloseTo(-122.38681, within(0.00001));
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

        assertThat(found).isNotNull();
    }

    @Test
    public void shouldQueryByDistanceUsingFilter() {

        Restaurant restaurant = new Restaurant("San Francisco International Airport (SFO)",
                new Location(37.61649, -122.38681), 94128);
        session.save(restaurant);
        session.clear();

        Filter filter = new Filter(new DistanceComparison(new DistanceFromPoint(37.61649, -122.38681, 1000 * 1000.0)), ComparisonOperator.LESS_THAN);
        Collection<Restaurant> found = session.loadAll(Restaurant.class, filter);
        assertThat(found).isNotNull();
        assertThat(found.size() >= 1).isTrue();
    }

    @Test
    public void saveAndRetrieveRestaurantWithLocation() {

        Restaurant restaurant = new Restaurant("San Francisco International Airport (SFO)",
                new Location(37.61649, -122.38681), 94128);

        session.save(restaurant);
        session.clear();

        Collection<Restaurant> results = session.loadAll(Restaurant.class,
                new Filter("name", ComparisonOperator.EQUALS, "San Francisco International Airport (SFO)"));
        assertThat(results).hasSize(1);
        Restaurant result = results.iterator().next();
        assertThat(result.getLocation()).isNotNull();
        assertThat(result.getLocation().getLatitude()).isCloseTo(37.61649, within(0d));
        assertThat(result.getLocation().getLongitude()).isCloseTo(-122.38681, within(0d));
    }

    /**
     * @see issue #159
     */
    @Test
    public void shouldSyncMappedLabelsFromEntityToTheNode_and_NodeToEntity() {

        Restaurant restaurant = new Restaurant();
        restaurant.setName("House of Mushroom & Pepperoni");
        List<String> labels = new ArrayList<>();
        labels.add("Delicious");
        labels.add("Ambiance");
        labels.add("Convenience");
        restaurant.labels = labels;

        session.save(restaurant);
        session.clear();

        Restaurant loaded = session.load(Restaurant.class, restaurant.getId());
        assertThat(loaded.labels.contains("Delicious")).isTrue();
        assertThat(loaded.labels.contains("Ambiance")).isTrue();
        assertThat(loaded.labels.contains("Convenience")).isTrue();
        assertThat(loaded.labels).hasSize(3);
    }

    @Test
    public void shouldUpdateLabelsCorrectly() throws Exception {
        Franchise franchise = new Franchise();

        Restaurant r1 = new Restaurant();
        r1.setName("La Strada Tooting");
        r1.labels = newArrayList("Delicious", "Foreign");

        Restaurant r2 = new Restaurant();
        r2.setName("La Strada Brno");
        r2.labels = newArrayList("Average", "Foreign");

        franchise.addBranch(new Branch(new Location(0.0, 0.0), franchise, r1));
        franchise.addBranch(new Branch(new Location(0.0, 0.0), franchise, r2));

        session.save(franchise);

        // remove labels, different label for each entity
        r1.labels = newArrayList("Foreign");
        r2.labels = newArrayList("Foreign");
        session.save(franchise);

        session.clear();

        Restaurant loadedR1 = session.load(Restaurant.class, r1.getId());
        assertThat(loadedR1.labels).containsOnly("Foreign");

        Restaurant loadedR2 = session.load(Restaurant.class, r2.getId());
        assertThat(loadedR2.labels).containsOnly("Foreign");
    }

    /**
     * @see issue #137
     */
    @Test
    public void shouldProvideUniqueParameterNamesForFilters() {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("La Cocina De Flaming Lips");
        session.save(restaurant);

        Restaurant another = new Restaurant();
        another.setName("Antica Pesa");
        session.save(another);

        Filters filters = new Filters();

        Filter firstFilter = new Filter("name", ComparisonOperator.EQUALS, "Foobar");
        firstFilter.setBooleanOperator(BooleanOperator.OR);
        filters.add(firstFilter);

        Filter secondFilter = new Filter("name", ComparisonOperator.EQUALS, "Antica Pesa");
        secondFilter.setBooleanOperator(BooleanOperator.OR);
        filters.add(secondFilter);

        Collection<Restaurant> results = session.loadAll(Restaurant.class, filters);
        assertThat(results.size() >= 1).isTrue();
    }

    /**
     * @see DATAGRAPH-904
     */
    @Test
    public void shouldFilterByIsNullOrNotNull() {
        Restaurant kuroda = new Restaurant("Kuroda", "Mainly Ramen");
        session.save(kuroda);

        Restaurant cyma = new Restaurant();
        cyma.setName("Cyma");
        session.save(cyma);

        session.clear();

        Filter descriptionIsNull = new Filter("description", ComparisonOperator.IS_NULL, null);
        Collection<Restaurant> results = session.loadAll(Restaurant.class, new Filters().add(descriptionIsNull));

        assertThat(results).hasSize(1);
        Restaurant restaurant = results.iterator().next();
        assertThat(restaurant.getName()).isEqualTo("Cyma");

        Filter descriptionIsNotNull = new Filter("description", ComparisonOperator.IS_NULL, null);
        descriptionIsNotNull.setNegated(true);
        results = session.loadAll(Restaurant.class, new Filters().add(descriptionIsNotNull));

        assertThat(results).hasSize(1);
        restaurant = results.iterator().next();
        assertThat(restaurant.getName()).isEqualTo("Kuroda");
    }

    /**
     * @see DATAGRAPH-904
     */
    @Test
    public void shouldFilterByPropertyWithConverter() {
        Restaurant kuroda = new Restaurant("Kuroda", 72.4);
        kuroda.setLaunchDate(new Date(1000));
        session.save(kuroda);

        Restaurant cyma = new Restaurant("Cyma", 80.5);
        cyma.setLaunchDate(new Date(2000));
        session.save(cyma);

        Filter launchDateFilter = new Filter("launchDate", ComparisonOperator.LESS_THAN, new Date(1001));

        Collection<Restaurant> results = session.loadAll(Restaurant.class, new Filters().add(launchDateFilter));
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.iterator().next().getName()).isEqualTo("Kuroda");

        Filter anotherFilter = new Filter("launchDate", ComparisonOperator.EQUALS, new Date(999));
        results = session.loadAll(Restaurant.class, new Filters().add(anotherFilter));

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    /**
     * @see DATAGRAPH-904
     */
    @Test
    public void shouldFilterByPropertyStartingWith() {
        Restaurant sfo = new Restaurant("San Francisco International Airport (SFO)", 72.4);
        sfo.setLaunchDate(new Date(1000));
        session.save(sfo);

        Restaurant kuroda = new Restaurant("Kuroda", 80.5);
        kuroda.setLaunchDate(new Date(2000));
        session.save(kuroda);

        Filter filter = new Filter("name", ComparisonOperator.STARTING_WITH, "San Francisco");

        Collection<Restaurant> results = session.loadAll(Restaurant.class, new Filters().add(filter));
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.iterator().next().getName()).isEqualTo("San Francisco International Airport (SFO)");
    }

    /**
     * @see DATAGRAPH-904
     */
    @Test
    public void shouldFilterByPropertyEndingWith() {
        Restaurant sfo = new Restaurant("San Francisco International Airport (SFO)", 72.4);
        sfo.setLaunchDate(new Date(1000));
        session.save(sfo);

        Restaurant kuroda = new Restaurant("Kuroda", 80.5);
        kuroda.setLaunchDate(new Date(2000));
        session.save(kuroda);

        Filter filter = new Filter("name", ComparisonOperator.ENDING_WITH, "Airport (SFO)");

        Collection<Restaurant> results = session.loadAll(Restaurant.class, new Filters().add(filter));
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.iterator().next().getName()).isEqualTo("San Francisco International Airport (SFO)");
    }

    /**
     * @see DATAGRAPH-904
     */
    @Test
    public void shouldFilterByPropertyContaining() {
        Restaurant sfo = new Restaurant("San Francisco International Airport (SFO)", 72.4);
        sfo.setLaunchDate(new Date(1000));
        session.save(sfo);

        Restaurant kuroda = new Restaurant("Kuroda", 80.5);
        kuroda.setLaunchDate(new Date(2000));
        session.save(kuroda);

        Filter filter = new Filter("name", ComparisonOperator.CONTAINING, "International Airport");

        Collection<Restaurant> results = session.loadAll(Restaurant.class, new Filters().add(filter));
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.iterator().next().getName()).isEqualTo("San Francisco International Airport (SFO)");
    }

    /**
     * @see DATAGRAPH-904
     */
    @Test
    public void shouldFilterByPropertyIn() {
        Restaurant sfo = new Restaurant("San Francisco International Airport (SFO)", 72.4);
        sfo.setLaunchDate(new Date(1000));
        session.save(sfo);

        Restaurant kuroda = new Restaurant("Kuroda", 80.5);
        kuroda.setLaunchDate(new Date(2000));
        session.save(kuroda);

        Filter filter = new Filter("name", ComparisonOperator.IN, new String[]{"Kuroda", "Foo", "Bar"});

        Collection<Restaurant> results = session.loadAll(Restaurant.class, new Filters().add(filter));
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.iterator().next().getName()).isEqualTo("Kuroda");
    }

    /**
     * @see DATAGRAPH-904
     */
    @Test
    public void shouldFilterByPropertyExists() {
        Restaurant sfo = new Restaurant("San Francisco International Airport (SFO)", 72.4);
        sfo.setLaunchDate(new Date(1000));
        session.save(sfo);

        Restaurant kuroda = new Restaurant("Kuroda", 80.5);
        kuroda.setLaunchDate(new Date(2000));
        session.save(kuroda);

        Filter exists = new Filter("name", ComparisonOperator.EXISTS);

        Collection<Restaurant> results = session.loadAll(Restaurant.class, new Filters().add(exists));
        assertThat(results).isNotNull();
        assertThat(results).hasSize(2);

        Filter notExists = new Filter("name", ComparisonOperator.EXISTS);
        notExists.setNegated(true);

        results = session.loadAll(Restaurant.class, new Filters().add(notExists));
        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    /**
     * @see DATAGRAPH-904
     */
    @Test
    public void shouldFilterByPropertyIsTrue() {

        Restaurant kazan = new Restaurant("Kazan", 77.0);
        kazan.setHalal(true);
        session.save(kazan);

        Restaurant kuroda = new Restaurant("Kuroda", 72.4);
        kuroda.setHalal(false);
        session.save(kuroda);

        Filter isHalal = new Filter("halal", ComparisonOperator.IS_TRUE);

        Collection<Restaurant> results = session.loadAll(Restaurant.class, new Filters().add(isHalal));
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.iterator().next().getName()).isEqualTo("Kazan");

        Filter notHalal = new Filter("halal", ComparisonOperator.IS_TRUE);
        notHalal.setNegated(true);

        results = session.loadAll(Restaurant.class, new Filters().add(notHalal));
        assertThat(results).isNotNull();
        assertThat(results).hasSize(1);
        assertThat(results.iterator().next().getName()).isEqualTo("Kuroda");
    }
}
