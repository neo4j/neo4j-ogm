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

package org.neo4j.ogm.persistence.session.capability;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Rating;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.testutil.TestUtils;

/**
 * @author Luanne Misquitta
 */
public class QueryCapabilityTest extends MultiDriverTestClass {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(baseConfiguration, "org.neo4j.ogm.domain.cineasts.annotated").openSession();
        session.purgeDatabase();
        session.clear();
        importCineasts();
    }

    private void importCineasts() {
        session.query(TestUtils.readCQLFile("org/neo4j/ogm/cql/cineasts.cql").toString(), Utils.map());
    }

    @After
    public void clearDatabase() {
        session.purgeDatabase();
    }

    /**
     * @see DATAGRAPH-697
     */
    @Test
    public void shouldQueryForArbitraryDataUsingBespokeParameterisedCypherQuery() {
        session.save(new Actor("Helen Mirren"));
        Actor alec = new Actor("Alec Baldwin");
        session.save(alec);
        session.save(new Actor("Matt Damon"));

        Iterable<Map<String, Object>> resultsIterable = session.query("MATCH (a:Actor) WHERE ID(a)={param} RETURN a.name as name",
                Collections.<String, Object>singletonMap("param", alec.getId())); //make sure the change is backward compatible
        assertNotNull("Results are empty", resultsIterable);
        Map<String, Object> row = resultsIterable.iterator().next();
        assertEquals("Alec Baldwin", row.get("name"));

        Result results = session.query("MATCH (a:Actor) WHERE ID(a)={param} RETURN a.name as name",
                Collections.<String, Object>singletonMap("param", alec.getId()));
        assertNotNull("Results are empty", results);
        assertEquals("Alec Baldwin", results.iterator().next().get("name"));
    }


    /**
     * @see DATAGRAPH-697
     */
    @Test(expected = RuntimeException.class)
    public void readOnlyQueryMustBeReadOnly() {
        session.save(new Actor("Jeff"));
        session.query("MATCH (a:Actor) SET a.age={age}", MapUtil.map("age", 5), true);
    }


    /**
     * @see DATAGRAPH-697
     */
    @Test
    public void modifyingQueryShouldReturnStatistics() {
        session.save(new Actor("Jeff"));
        session.save(new Actor("John"));
        session.save(new Actor("Colin"));
        Result result = session.query("MATCH (a:Actor) SET a.age={age}", MapUtil.map("age", 5), false);
        assertNotNull(result);
        assertNotNull(result.queryStatistics());
        assertEquals(3, result.queryStatistics().getPropertiesSet());

        result = session.query("MATCH (a:Actor) SET a.age={age}", MapUtil.map("age", 5));
        assertNotNull(result);
        assertNotNull(result.queryStatistics());
        assertEquals(3, result.queryStatistics().getPropertiesSet());
    }

    /**
     * @see DATAGRAPH-697
     */
    @Test
    public void modifyingQueryShouldReturnResultsWithStatistics() {
        session.save(new Actor("Jeff"));
        session.save(new Actor("John"));
        session.save(new Actor("Colin"));
        Result result = session.query("MATCH (a:Actor) SET a.age={age} RETURN a.name", MapUtil.map("age", 5), false);
        assertNotNull(result);
        assertNotNull(result.queryStatistics());
        assertEquals(3, result.queryStatistics().getPropertiesSet());
        List<String> names = new ArrayList<>();

        Iterator<Map<String, Object>> namesIterator = result.queryResults().iterator();
        while (namesIterator.hasNext()) {
            names.add((String) namesIterator.next().get("a.name"));
        }

        assertEquals(3, names.size());
        assertTrue(names.contains("Jeff"));
        assertTrue(names.contains("John"));
        assertTrue(names.contains("Colin"));

        result = session.query("MATCH (a:Actor) SET a.age={age} RETURN a.name, a.age", MapUtil.map("age", 5));
        assertNotNull(result);
        assertNotNull(result.queryStatistics());
        assertEquals(3, result.queryStatistics().getPropertiesSet());
        names = new ArrayList<>();

        namesIterator = result.queryResults().iterator();
        while (namesIterator.hasNext()) {
            Map<String, Object> row = namesIterator.next();
            names.add((String) row.get("a.name"));
            assertEquals(5l, ((Number) row.get("a.age")).longValue());
        }

        assertEquals(3, names.size());
        assertTrue(names.contains("Jeff"));
        assertTrue(names.contains("John"));
        assertTrue(names.contains("Colin"));
    }

    /**
     * @see DATAGRAPH-697
     */
    @Test
    public void readOnlyQueryShouldNotReturnStatistics() {
        session.save(new Actor("Jeff"));
        session.save(new Actor("John"));
        session.save(new Actor("Colin"));
        Result result = session.query("MATCH (a:Actor) RETURN a.name", Collections.EMPTY_MAP, true);
        assertNotNull(result);
        assertNull(result.queryStatistics());

        List<String> names = new ArrayList<>();

        Iterator<Map<String, Object>> namesIterator = result.queryResults().iterator();
        while (namesIterator.hasNext()) {
            names.add((String) namesIterator.next().get("a.name"));
        }

        assertEquals(3, names.size());
        assertTrue(names.contains("Jeff"));
        assertTrue(names.contains("John"));
        assertTrue(names.contains("Colin"));
    }

    /**
     * @see DATAGRAPH-697
     */
    @Test
    public void modifyingQueryShouldBePermittedWhenQueryingForObject() {
        session.save(new Actor("Jeff"));
        session.save(new Actor("John"));
        session.save(new Actor("Colin"));
        Actor jeff = session.queryForObject(Actor.class, "MATCH (a:Actor {name:{name}}) set a.age={age} return a", MapUtil.map("name", "Jeff", "age", 40));
        assertNotNull(jeff);
        assertEquals("Jeff", jeff.getName());
    }

    /**
     * @see DATAGRAPH-697
     */
    @Test
    public void modifyingQueryShouldBePermittedWhenQueryingForObjects() {
        session.save(new Actor("Jeff"));
        session.save(new Actor("John"));
        session.save(new Actor("Colin"));
        Iterable<Actor> actors = session.query(Actor.class, "MATCH (a:Actor) set a.age={age} return a", MapUtil.map("age", 40));
        assertNotNull(actors);

        List<String> names = new ArrayList<>();

        Iterator<Actor> actorIterator = actors.iterator();
        while (actorIterator.hasNext()) {
            names.add(actorIterator.next().getName());
        }

        assertEquals(3, names.size());
        assertTrue(names.contains("Jeff"));
        assertTrue(names.contains("John"));
        assertTrue(names.contains("Colin"));
    }

    @Test
    public void shouldBeAbleToHandleNullValuesInQueryResults() {
        session.save(new Actor("Jeff"));
        Iterable<Map<String, Object>> results = session.query("MATCH (a:Actor) return a.nonExistent as nonExistent", Collections.EMPTY_MAP);
        Map<String, Object> result = results.iterator().next();
        assertNull(result.get("nonExistent"));
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapEntities() {
        Iterator<Map<String, Object>> results = session.query("MATCH (u:User {name:{name}})-[:RATED]->(m) RETURN u as user, m as movie", MapUtil.map("name", "Vince")).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);
        User user = (User) result.get("user");
        assertNotNull(user);
        Movie movie = (Movie) result.get("movie");
        assertNotNull(movie);
        assertEquals("Vince", user.getName());
        assertEquals("Top Gear", movie.getTitle());
        assertFalse(results.hasNext());
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapEntitiesAndScalars() {
        Iterator<Map<String, Object>> results = session.query("MATCH (u:User {name:{name}})-[:RATED]->(m) RETURN u as user, count(m) as count", MapUtil.map("name", "Michal")).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);
        User user = (User) result.get("user");
        assertNotNull(user);
        assertEquals("Michal", user.getName());
        Number count = (Number) result.get("count");
        assertEquals(2L, count.longValue());
        assertFalse(results.hasNext());
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapEntitiesAndScalarsMultipleRows() {
        Iterator<Map<String, Object>> results = session.query("MATCH (u:User)-[r:RATED]->(m) RETURN m as movie, avg(r.stars) as average ORDER BY average DESC", Collections.EMPTY_MAP).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);

        Movie movie = (Movie) result.get("movie");
        assertNotNull(movie);
        assertEquals("Pulp Fiction", movie.getTitle());
        Number avg = (Number) result.get("average");
        assertEquals(5.0, avg);

        result = results.next();

        movie = (Movie) result.get("movie");
        assertNotNull(movie);
        assertEquals("Top Gear", movie.getTitle());
        avg = (Number) result.get("average");
        assertEquals(3.5, avg);

        assertFalse(results.hasNext());
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapEntitiesAndScalarsMultipleRowsAndNoAlias() {
        Iterator<Map<String, Object>> results = session.query("MATCH (u:User)-[r:RATED]->(m) RETURN m, avg(r.stars) ORDER BY avg(r.stars) DESC", Collections.EMPTY_MAP).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);

        Movie movie = (Movie) result.get("m");
        assertNotNull(movie);
        assertEquals("Pulp Fiction", movie.getTitle());
        Number avg = (Number) result.get("avg(r.stars)");
        assertEquals(5.0, avg);

        result = results.next();

        movie = (Movie) result.get("m");
        assertNotNull(movie);
        assertEquals("Top Gear", movie.getTitle());
        avg = (Number) result.get("avg(r.stars)");
        assertEquals(3.5, avg);

        assertFalse(results.hasNext());
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapEntitiesAndRelationships() {
        Iterator<Map<String, Object>> results = session.query("MATCH (u:User {name:{name}})-[r:FRIENDS]->(friend) RETURN u as user, friend as friend, r", MapUtil.map("name", "Michal")).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);

        User user = (User) result.get("user");
        assertNotNull(user);
        assertEquals("Michal", user.getName());

        User friend = (User) result.get("friend");
        assertNotNull(friend);
        assertEquals("Vince", friend.getName());

        assertEquals(friend.getName(), user.getFriends().iterator().next().getName());

        assertFalse(results.hasNext());
    }


    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapEntitiesAndRelationshipsOfDifferentTypes() {
        Iterator<Map<String, Object>> results = session.query("MATCH (u:User {name:{name}})-[r:FRIENDS]->(friend)-[r2:RATED]->(m) RETURN u as user, friend as friend, r, r2, m as movie, r2.stars as stars", MapUtil.map("name", "Michal")).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);

        User user = (User) result.get("user");
        assertNotNull(user);
        assertEquals("Michal", user.getName());

        User friend = (User) result.get("friend");
        assertNotNull(friend);
        assertEquals("Vince", friend.getName());

        assertEquals(friend.getName(), user.getFriends().iterator().next().getName());

        Movie topGear = (Movie) result.get("movie");
        assertNotNull(topGear);
        assertEquals("Top Gear", topGear.getTitle());

        assertEquals(1, friend.getRatings().size());
        assertEquals(topGear.getTitle(), friend.getRatings().iterator().next().getMovie().getTitle());
        Number stars = (Number) result.get("stars");
        assertEquals(4L, stars.longValue());

        assertFalse(results.hasNext());
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapRelationshipEntities() {
        Iterator<Map<String, Object>> results = session.query("MATCH (u:User {name:{name}})-[r:RATED]->(m) RETURN u,r,m", MapUtil.map("name", "Vince")).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);

        Movie movie = (Movie) result.get("m");
        assertNotNull(movie);
        assertEquals("Top Gear", movie.getTitle());

        User user = (User) result.get("u");
        assertNotNull(user);
        assertEquals("Vince", user.getName());

        Rating rating = (Rating) result.get("r");
        assertNotNull(rating);
        assertEquals(4, rating.getStars());

        assertEquals(rating.getId(), movie.getRatings().iterator().next().getId());
        assertEquals(rating.getId(), user.getRatings().iterator().next().getId());

        assertFalse(results.hasNext());
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapVariableDepthRelationshipsWithIncompletePaths() {
        Iterator<Map<String, Object>> results = session.query("match (u:User {name:{name}}) match (m:Movie {title:{title}}) match (u)-[r*0..2]-(m) return u,r,m", MapUtil.map("name", "Vince", "title", "Top Gear")).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);

        /*
            Expect 2 rows:
             one with (vince)-[:FRIENDS]-(michal)-[:RATED]-(topgear) where FRIENDS cannot be mapped because michal isn't known
             one with (vince)-[:RATED]-(top gear) where RATED can be mapped
         */
        boolean ratedRelationshipFound = false;
        Movie movie = (Movie) result.get("m");
        assertNotNull(movie);
        assertEquals("Top Gear", movie.getTitle());

        User user = (User) result.get("u");
        assertNotNull(user);
        assertEquals("Vince", user.getName());

        List<Rating> ratings = (List) result.get("r");
        if (ratings.size() == 1) { //because the list of ratings with size 2= friends,rated relationships
            Rating rating = ratings.get(0);
            assertNotNull(rating);
            assertEquals(4, rating.getStars());
            assertEquals(rating.getId(), movie.getRatings().iterator().next().getId());
            assertEquals(rating.getId(), user.getRatings().iterator().next().getId());
            ratedRelationshipFound = true;
        }

        assertNull(user.getFriends());

        result = results.next();
        assertNotNull(result);

        movie = (Movie) result.get("m");
        assertNotNull(movie);
        assertEquals("Top Gear", movie.getTitle());

        user = (User) result.get("u");
        assertNotNull(user);
        assertEquals("Vince", user.getName());

        ratings = (List) result.get("r");
        if (ratings.size() == 1) { //because the list of ratings with size 2= friends,rated relationships
            Rating rating = ratings.get(0);
            assertNotNull(rating);
            assertEquals(4, rating.getStars());
            assertEquals(rating.getId(), movie.getRatings().iterator().next().getId());
            assertEquals(rating.getId(), user.getRatings().iterator().next().getId());
            ratedRelationshipFound = true;
        }
        assertTrue(ratedRelationshipFound);
        assertNull(user.getFriends());
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapVariableDepthRelationshipsWithCompletePaths() {
        Iterator<Map<String, Object>> results = session.query("match (u:User {name:{name}}) match (u)-[r*0..1]-(n) return u,r,n", MapUtil.map("name", "Vince")).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);
        boolean foundMichal = false;
        /*
            Expect 3 rows:
             one with (vince)-[:FRIENDS]-(michal)
             one with (vince)-[:RATED]-(top gear)
             one with (vince)
         */

        User user = (User) result.get("u");
        assertNotNull(user);
        assertEquals("Vince", user.getName());
        foundMichal = checkForMichal(result, foundMichal);

        result = results.next();
        assertNotNull(result);
        foundMichal = checkForMichal(result, foundMichal);

        result = results.next();
        assertNotNull(result);
        foundMichal = checkForMichal(result, foundMichal);

        assertTrue(foundMichal);
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapCollectionsOfNodes() {
        Iterator<Map<String, Object>> results = session.query("match (u:User {name:{name}})-[r:RATED]->(m) return u as user,collect(r), collect(m) as movies", MapUtil.map("name", "Michal")).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);
        assertEquals("Michal", ((User) result.get("user")).getName());

        List<Rating> ratings = (List) result.get("collect(r)");
        assertEquals(2, ratings.size());
        for (Rating rating : ratings) {
            assertEquals("Michal", rating.getUser().getName());
        }

        List<Movie> movies = (List) result.get("movies");
        assertEquals(2, movies.size());
        for (Movie movie : movies) {
            if (movie.getRatings().iterator().next().getStars() == 3) {
                assertEquals("Top Gear", movie.getTitle());
            } else {
                assertEquals("Pulp Fiction", movie.getTitle());
                assertEquals(5, movie.getRatings().iterator().next().getStars());
            }
        }
        assertFalse(results.hasNext());
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapCollectionsFromPath() {
        Iterator<Map<String, Object>> results = session.query("MATCH p=(u:User {name:{name}})-[r:RATED]->(m) RETURN nodes(p) as nodes, rels(p) as rels", MapUtil.map("name", "Vince")).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);

        List<Object> nodes = (List) result.get("nodes");
        List<Object> rels = (List) result.get("rels");
        assertEquals(2, nodes.size());
        assertEquals(1, rels.size());

        for (Object o : nodes) {
            if (o instanceof User) {
                User user = (User) o;
                assertEquals("Vince", user.getName());
                assertEquals(1, user.getRatings().size());
                Movie movie = user.getRatings().iterator().next().getMovie();
                assertNotNull(movie);
                assertEquals("Top Gear", movie.getTitle());
                Rating rating = movie.getRatings().iterator().next();
                assertNotNull(rating);
                assertEquals(4, rating.getStars());
            }
        }
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapArrays() {
        Iterator<Map<String, Object>> results = session.query("MATCH (u:User {name:{name}}) RETURN u.array as arr", MapUtil.map("name", "Christophe")).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);
        assertEquals(2, ((String[]) result.get("arr")).length);
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapMixedArrays() {
        Iterator<Map<String, Object>> results = session.query("MATCH (u:User {name:{name}}) RETURN u.array as arr, [1,'two',true] as mixed", MapUtil.map("name", "Christophe")).iterator();
        assertNotNull(results);
        Map<String, Object> result = results.next();
        assertNotNull(result);
        assertEquals(2, ((String[]) result.get("arr")).length);
        Object[] mixed = (Object[]) result.get("mixed");
        assertEquals(3, mixed.length);
        assertEquals(1L, ((Number) mixed[0]).longValue());
        assertEquals("two", mixed[1]);
        assertEquals(true, mixed[2]);
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void modifyingQueryShouldBeAbleToMapEntitiesAndReturnStatistics() {
        Result result = session.query("MATCH (u:User {name:{name}})-[:RATED]->(m) WITH u,m SET u.age={age} RETURN u as user, m as movie", MapUtil.map("name", "Vince", "age", 20));
        Iterator<Map<String, Object>> results = result.queryResults().iterator();
        assertNotNull(results);
        Map<String, Object> row = results.next();
        assertNotNull(row);
        User user = (User) row.get("user");
        assertNotNull(user);
        Movie movie = (Movie) row.get("movie");
        assertNotNull(movie);
        assertEquals("Vince", user.getName());
        assertEquals("Top Gear", movie.getTitle());
        assertFalse(results.hasNext());
    }


    /**
     * @see Issue 136
     */
    @Test
    public void shouldNotOverflowIntegers() {
        long start = Integer.MAX_VALUE;
        session.query("CREATE (n:Sequence {id:{id}, next:{start}})", MapUtil.map("id", "test", "start", start));

        String incrementStmt = "MATCH (n:Sequence) WHERE n.id = {id} REMOVE n.lock SET n.next = n.next + {increment} RETURN n.next - {increment} as current";

        Result result = session.query(incrementStmt, MapUtil.map("id", "test", "increment", 1));
        assertEquals(start, ((Number) result.iterator().next().get("current")).longValue());

        result = session.query(incrementStmt, MapUtil.map("id", "test", "increment", 1));

        //expected:<2147483648> but was:<-2147483648>
        assertEquals(start + 1, ((Number) result.iterator().next().get("current")).longValue());
    }

    /**
     * @see Issue 150
     */
    @Test
    public void shouldLoadNodesWithUnmappedOrNoLabels() {
        int movieCount = 0, userCount = 0, unmappedCount = 0, noLabelCount = 0;

        session.query("CREATE (unknown), (m:Unmapped), (n:Movie), (n)-[:UNKNOWN]->(m)", Collections.EMPTY_MAP);

        Result result = session.query("MATCH (n) return n", Collections.EMPTY_MAP);
        assertNotNull(result);

        Iterator<Map<String, Object>> resultIterator = result.iterator();
        while (resultIterator.hasNext()) {
            Map<String, Object> row = resultIterator.next();
            Object n = row.get("n");
            if (n instanceof User) {
                userCount++;
            } else if (n instanceof Movie) {
                movieCount++;
            } else if (n instanceof NodeModel) {
                if (((NodeModel) n).getLabels().length == 0) {
                    noLabelCount++;
                } else if (((NodeModel) n).getLabels()[0].equals("Unmapped")) {
                    unmappedCount++;
                }
            }
        }
        assertEquals(1, unmappedCount);
        assertEquals(1, noLabelCount);
        assertEquals(4, movieCount);
        assertEquals(4, userCount);
    }

    /**
     * @see Issue 148
     */
    @Test
    public void shouldMapCypherCollectionsToArrays() {
        Iterator<Map<String, Object>> iterator = session.query("MATCH (n:User) return collect(n.name) as names", Collections.EMPTY_MAP).iterator();
        assertTrue(iterator.hasNext());
        Map<String, Object> row = iterator.next();
        assertTrue(row.get("names").getClass().isArray());
        assertEquals(4, ((String[]) row.get("names")).length);

        iterator = session.query("MATCH (n:User {name:'Michal'}) return collect(n.name) as names", Collections.EMPTY_MAP).iterator();
        assertTrue(iterator.hasNext());
        row = iterator.next();
        assertTrue(row.get("names").getClass().isArray());
        assertEquals(1, ((String[]) row.get("names")).length);

        iterator = session.query("MATCH (n:User {name:'Does Not Exist'}) return collect(n.name) as names", Collections.EMPTY_MAP).iterator();
        assertTrue(iterator.hasNext());
        row = iterator.next();
        assertTrue(row.get("names").getClass().isArray());
        assertEquals(0, ((Object[]) row.get("names")).length);
    }

    private boolean checkForMichal(Map<String, Object> result, boolean foundMichal) {
        if (result.get("n") instanceof User) {
            User u = (User) result.get("n");
            if (u.getName().equals("Michal")) {
                assertEquals(1, u.getFriends().size());
                assertEquals("Vince", u.getFriends().iterator().next().getName());
                foundMichal = true;
            }
        }
        return foundMichal;
    }
}
