/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.persistence.session.capability;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.ExtendedUser;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Rating;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.domain.linkedlist.Item;
import org.neo4j.ogm.domain.nested.NestingClass;
import org.neo4j.ogm.domain.restaurant.Restaurant;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.exception.core.MappingException;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.testutil.TestUtils;

/**
 * @author Andreas Berger
 * @author Atul Mahind
 * @author Gerrit Meier
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class QueryCapabilityTest extends MultiDriverTestClass {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory(driver, "org.neo4j.ogm.domain.cineasts.annotated",
            "org.neo4j.ogm.domain.linkedlist").openSession();
        session = new SessionFactory(driver,
            "org.neo4j.ogm.domain.cineasts.annotated",
            "org.neo4j.ogm.domain.nested",
            "org.neo4j.ogm.domain.linkedlist"
        ).openSession();
        session.purgeDatabase();
        session.clear();
        importCineasts();
        importFriendships();
    }

    private void importCineasts() {
        session.query(TestUtils.readCQLFile("org/neo4j/ogm/cql/cineasts.cql").toString(), Utils.map());
    }

    private void importFriendships() {
        session.query(TestUtils.readCQLFile("org/neo4j/ogm/cql/items.cql").toString(), Utils.map());
    }

    @After
    public void clearDatabase() {
        session.purgeDatabase();
    }

    @Test // DATAGRAPH-697
    public void shouldQueryForArbitraryDataUsingBespokeParameterisedCypherQuery() {
        session.save(new Actor("Helen Mirren"));
        Actor alec = new Actor("Alec Baldwin");
        session.save(alec);
        session.save(new Actor("Matt Damon"));

        Iterable<Map<String, Object>> resultsIterable = session
            .query("MATCH (a:Actor) WHERE ID(a)={param} RETURN a.name as name",
                Collections.<String, Object>singletonMap("param",
                    alec.getId())); //make sure the change is backward compatible
        assertThat(resultsIterable).as("Results are empty").isNotNull();
        Map<String, Object> row = resultsIterable.iterator().next();
        assertThat(row.get("name")).isEqualTo("Alec Baldwin");

        Result results = session.query("MATCH (a:Actor) WHERE ID(a)={param} RETURN a.name as name",
            Collections.<String, Object>singletonMap("param", alec.getId()));
        assertThat(results).as("Results are empty").isNotNull();
        assertThat(results.iterator().next().get("name")).isEqualTo("Alec Baldwin");
    }

    @Test(expected = RuntimeException.class) // DATAGRAPH-697
    public void readOnlyQueryMustBeReadOnly() {
        session.save(new Actor("Jeff"));
        session.query("MATCH (a:Actor) SET a.age={age}", MapUtil.map("age", 5), true);
    }

    @Test // DATAGRAPH-697
    public void modifyingQueryShouldReturnStatistics() {
        session.save(new Actor("Jeff"));
        session.save(new Actor("John"));
        session.save(new Actor("Colin"));
        Result result = session.query("MATCH (a:Actor) SET a.age={age}", MapUtil.map("age", 5), false);
        assertThat(result).isNotNull();
        assertThat(result.queryStatistics()).isNotNull();
        assertThat(result.queryStatistics().getPropertiesSet()).isEqualTo(3);

        result = session.query("MATCH (a:Actor) SET a.age={age}", MapUtil.map("age", 5));
        assertThat(result).isNotNull();
        assertThat(result.queryStatistics()).isNotNull();
        assertThat(result.queryStatistics().getPropertiesSet()).isEqualTo(3);
    }

    @Test // DATAGRAPH-697
    public void modifyingQueryShouldReturnResultsWithStatistics() {
        session.save(new Actor("Jeff"));
        session.save(new Actor("John"));
        session.save(new Actor("Colin"));
        Result result = session.query("MATCH (a:Actor) SET a.age={age} RETURN a.name", MapUtil.map("age", 5), false);
        assertThat(result).isNotNull();
        assertThat(result.queryStatistics()).isNotNull();
        assertThat(result.queryStatistics().getPropertiesSet()).isEqualTo(3);
        List<String> names = new ArrayList<>();

        Iterator<Map<String, Object>> namesIterator = result.queryResults().iterator();
        while (namesIterator.hasNext()) {
            names.add((String) namesIterator.next().get("a.name"));
        }

        assertThat(names).hasSize(3);
        assertThat(names.contains("Jeff")).isTrue();
        assertThat(names.contains("John")).isTrue();
        assertThat(names.contains("Colin")).isTrue();

        result = session.query("MATCH (a:Actor) SET a.age={age} RETURN a.name, a.age", MapUtil.map("age", 5));
        assertThat(result).isNotNull();
        assertThat(result.queryStatistics()).isNotNull();
        assertThat(result.queryStatistics().getPropertiesSet()).isEqualTo(3);
        names = new ArrayList<>();

        namesIterator = result.queryResults().iterator();
        while (namesIterator.hasNext()) {
            Map<String, Object> row = namesIterator.next();
            names.add((String) row.get("a.name"));
            assertThat(((Number) row.get("a.age")).longValue()).isEqualTo(5L);
        }

        assertThat(names).hasSize(3);
        assertThat(names.contains("Jeff")).isTrue();
        assertThat(names.contains("John")).isTrue();
        assertThat(names.contains("Colin")).isTrue();
    }

    @Test // DATAGRAPH-697
    public void readOnlyQueryShouldNotReturnStatistics() {
        session.save(new Actor("Jeff"));
        session.save(new Actor("John"));
        session.save(new Actor("Colin"));
        Result result = session.query("MATCH (a:Actor) RETURN a.name", emptyMap(), true);
        assertThat(result).isNotNull();
        assertThat(result.queryStatistics()).isNull();

        List<String> names = new ArrayList<>();

        Iterator<Map<String, Object>> namesIterator = result.queryResults().iterator();
        while (namesIterator.hasNext()) {
            names.add((String) namesIterator.next().get("a.name"));
        }

        assertThat(names).hasSize(3);
        assertThat(names.contains("Jeff")).isTrue();
        assertThat(names.contains("John")).isTrue();
        assertThat(names.contains("Colin")).isTrue();
    }

    @Test // DATAGRAPH-697
    public void modifyingQueryShouldBePermittedWhenQueryingForObject() {
        session.save(new Actor("Jeff"));
        session.save(new Actor("John"));
        session.save(new Actor("Colin"));
        Actor jeff = session.queryForObject(Actor.class, "MATCH (a:Actor {name:{name}}) set a.age={age} return a",
            MapUtil.map("name", "Jeff", "age", 40));
        assertThat(jeff).isNotNull();
        assertThat(jeff.getName()).isEqualTo("Jeff");
    }

    @Test // DATAGRAPH-697
    public void modifyingQueryShouldBePermittedWhenQueryingForObjects() {
        session.save(new Actor("Jeff"));
        session.save(new Actor("John"));
        session.save(new Actor("Colin"));
        Iterable<Actor> actors = session
            .query(Actor.class, "MATCH (a:Actor) set a.age={age} return a", MapUtil.map("age", 40));
        assertThat(actors).isNotNull();

        List<String> names = new ArrayList<>();

        Iterator<Actor> actorIterator = actors.iterator();
        while (actorIterator.hasNext()) {
            names.add(actorIterator.next().getName());
        }

        assertThat(names).hasSize(3);
        assertThat(names.contains("Jeff")).isTrue();
        assertThat(names.contains("John")).isTrue();
        assertThat(names.contains("Colin")).isTrue();
    }

    @Test
    public void shouldBeAbleToHandleNullValuesInQueryResults() {
        session.save(new Actor("Jeff"));
        Iterable<Map<String, Object>> results = session
            .query("MATCH (a:Actor) return a.nonExistent as nonExistent", emptyMap());
        Map<String, Object> result = results.iterator().next();
        assertThat(result.get("nonExistent")).isNull();
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapEntities() {
        Iterator<Map<String, Object>> results = session
            .query("MATCH (u:User {name:{name}})-[:RATED]->(m) RETURN u as user, m as movie",
                MapUtil.map("name", "Vince")).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();
        User user = (User) result.get("user");
        assertThat(user).isNotNull();
        Movie movie = (Movie) result.get("movie");
        assertThat(movie).isNotNull();
        assertThat(user.getName()).isEqualTo("Vince");
        assertThat(movie.getTitle()).isEqualTo("Top Gear");
        assertThat(results.hasNext()).isFalse();
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapEntitiesAndScalars() {
        Iterator<Map<String, Object>> results = session
            .query("MATCH (u:User {name:{name}})-[:RATED]->(m) RETURN u as user, count(m) as count",
                MapUtil.map("name", "Michal")).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();
        User user = (User) result.get("user");
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("Michal");
        Number count = (Number) result.get("count");
        assertThat(count.longValue()).isEqualTo(2L);
        assertThat(results.hasNext()).isFalse();
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapEntitiesAndScalarsMultipleRows() {
        Iterator<Map<String, Object>> results = session
            .query("MATCH (u:User)-[r:RATED]->(m) RETURN m as movie, avg(r.stars) as average ORDER BY average DESC",
                emptyMap()).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();

        Movie movie = (Movie) result.get("movie");
        assertThat(movie).isNotNull();
        assertThat(movie.getTitle()).isEqualTo("Pulp Fiction");
        Number avg = (Number) result.get("average");
        assertThat(avg).isEqualTo(5.0);

        result = results.next();

        movie = (Movie) result.get("movie");
        assertThat(movie).isNotNull();
        assertThat(movie.getTitle()).isEqualTo("Top Gear");
        avg = (Number) result.get("average");
        assertThat(avg).isEqualTo(3.5);

        assertThat(results.hasNext()).isFalse();
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapEntitiesAndScalarsMultipleRowsAndNoAlias() {
        Iterator<Map<String, Object>> results = session
            .query("MATCH (u:User)-[r:RATED]->(m) RETURN m, avg(r.stars) ORDER BY avg(r.stars) DESC",
                emptyMap()).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();

        Movie movie = (Movie) result.get("m");
        assertThat(movie).isNotNull();
        assertThat(movie.getTitle()).isEqualTo("Pulp Fiction");
        Number avg = (Number) result.get("avg(r.stars)");
        assertThat(avg).isEqualTo(5.0);

        result = results.next();

        movie = (Movie) result.get("m");
        assertThat(movie).isNotNull();
        assertThat(movie.getTitle()).isEqualTo("Top Gear");
        avg = (Number) result.get("avg(r.stars)");
        assertThat(avg).isEqualTo(3.5);

        assertThat(results.hasNext()).isFalse();
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapEntitiesAndRelationships() {
        Iterator<Map<String, Object>> results = session
            .query("MATCH (u:User {name:{name}})-[r:FRIENDS]->(friend) RETURN u as user, friend as friend, r",
                MapUtil.map("name", "Michal")).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();

        User user = (User) result.get("user");
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("Michal");

        User friend = (User) result.get("friend");
        assertThat(friend).isNotNull();
        assertThat(friend.getName()).isEqualTo("Vince");

        assertThat(user.getFriends().iterator().next().getName()).isEqualTo(friend.getName());

        assertThat(results.hasNext()).isFalse();
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapEntitiesAndRelationshipsOfDifferentTypes() {
        Iterator<Map<String, Object>> results = session.query(
            "MATCH (u:User {name:{name}})-[r:FRIENDS]->(friend)-[r2:RATED]->(m) RETURN u as user, friend as friend, r, r2, m as movie, r2.stars as stars",
            MapUtil.map("name", "Michal")).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();

        User user = (User) result.get("user");
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("Michal");

        User friend = (User) result.get("friend");
        assertThat(friend).isNotNull();
        assertThat(friend.getName()).isEqualTo("Vince");

        assertThat(user.getFriends().iterator().next().getName()).isEqualTo(friend.getName());

        Movie topGear = (Movie) result.get("movie");
        assertThat(topGear).isNotNull();
        assertThat(topGear.getTitle()).isEqualTo("Top Gear");

        assertThat(friend.getRatings()).hasSize(1);
        assertThat(friend.getRatings().iterator().next().getMovie().getTitle()).isEqualTo(topGear.getTitle());
        Number stars = (Number) result.get("stars");
        assertThat(stars.longValue()).isEqualTo(4L);

        assertThat(results.hasNext()).isFalse();
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapRelationshipEntities() {
        Iterator<Map<String, Object>> results = session
            .query("MATCH (u:User {name:{name}})-[r:RATED]->(m) RETURN u,r,m", MapUtil.map("name", "Vince")).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();

        Movie movie = (Movie) result.get("m");
        assertThat(movie).isNotNull();
        assertThat(movie.getTitle()).isEqualTo("Top Gear");

        User user = (User) result.get("u");
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("Vince");

        Rating rating = (Rating) result.get("r");
        assertThat(rating).isNotNull();
        assertThat(rating.getStars()).isEqualTo(4);

        assertThat(movie.getRatings().iterator().next().getId()).isEqualTo(rating.getId());
        assertThat(user.getRatings().iterator().next().getId()).isEqualTo(rating.getId());

        assertThat(results.hasNext()).isFalse();
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapVariableDepthRelationshipsWithIncompletePaths() {
        Iterator<Map<String, Object>> results = session
            .query("match (u:User {name:{name}}) match (m:Movie {title:{title}}) match (u)-[r*0..2]-(m) return u,r,m",
                MapUtil.map("name", "Vince", "title", "Top Gear")).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();

        /*
            Expect 2 rows:
             one with (vince)-[:FRIENDS]-(michal)-[:RATED]-(topgear) where FRIENDS cannot be mapped because michal isn't known
             one with (vince)-[:RATED]-(top gear) where RATED can be mapped
         */
        boolean ratedRelationshipFound = false;
        Movie movie = (Movie) result.get("m");
        assertThat(movie).isNotNull();
        assertThat(movie.getTitle()).isEqualTo("Top Gear");

        User user = (User) result.get("u");
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("Vince");

        List<Rating> ratings = (List) result.get("r");
        if (ratings.size() == 1) { //because the list of ratings with size 2= friends,rated relationships
            Rating rating = ratings.get(0);
            assertThat(rating).isNotNull();
            assertThat(rating.getStars()).isEqualTo(4);
            assertThat(movie.getRatings().iterator().next().getId()).isEqualTo(rating.getId());
            assertThat(user.getRatings().iterator().next().getId()).isEqualTo(rating.getId());
            ratedRelationshipFound = true;
        }

        assertThat(user.getFriends()).isNull();

        result = results.next();
        assertThat(result).isNotNull();

        movie = (Movie) result.get("m");
        assertThat(movie).isNotNull();
        assertThat(movie.getTitle()).isEqualTo("Top Gear");

        user = (User) result.get("u");
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("Vince");

        ratings = (List) result.get("r");
        if (ratings.size() == 1) { //because the list of ratings with size 2= friends,rated relationships
            Rating rating = ratings.get(0);
            assertThat(rating).isNotNull();
            assertThat(rating.getStars()).isEqualTo(4);
            assertThat(movie.getRatings().iterator().next().getId()).isEqualTo(rating.getId());
            assertThat(user.getRatings().iterator().next().getId()).isEqualTo(rating.getId());
            ratedRelationshipFound = true;
        }
        assertThat(ratedRelationshipFound).isTrue();
        assertThat(user.getFriends()).isNull();
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapVariableDepthRelationshipsWithCompletePaths() {
        Iterator<Map<String, Object>> results = session
            .query("match (u:User {name:{name}}) match (u)-[r*0..1]-(n) return u,r,n", MapUtil.map("name", "Vince"))
            .iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();
        /*
            Expect at max 4 rows:
             one with (vince)-[:FRIENDS]-(michal)
             one with (vince)-[:RATED]-(top gear)
             one with (vince)-[:EXTENDED_FRIEND]->(extended)
             one with (vince)
         */

        User user = (User) result.get("u");
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("Vince");

        boolean foundMichal = checkForMichal(result);
        while (!foundMichal && results.hasNext()) {
            result = results.next();
            assertThat(result).isNotNull();
            foundMichal = checkForMichal(result);
        }

        assertThat(foundMichal).isTrue();
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapCollectionsOfNodes() {
        Iterator<Map<String, Object>> results = session
            .query("match (u:User {name:{name}})-[r:RATED]->(m) return u as user,collect(r), collect(m) as movies",
                MapUtil.map("name", "Michal")).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();
        assertThat(((User) result.get("user")).getName()).isEqualTo("Michal");

        List<Rating> ratings = (List) result.get("collect(r)");
        assertThat(ratings).hasSize(2);
        for (Rating rating : ratings) {
            assertThat(rating.getUser().getName()).isEqualTo("Michal");
        }

        List<Movie> movies = (List) result.get("movies");
        assertThat(movies).hasSize(2);
        for (Movie movie : movies) {
            if (movie.getRatings().iterator().next().getStars() == 3) {
                assertThat(movie.getTitle()).isEqualTo("Top Gear");
            } else {
                assertThat(movie.getTitle()).isEqualTo("Pulp Fiction");
                assertThat(movie.getRatings().iterator().next().getStars()).isEqualTo(5);
            }
        }
        assertThat(results.hasNext()).isFalse();
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapCollectionsFromPath() {
        Iterator<Map<String, Object>> results = session
            .query("MATCH p=(u:User {name:{name}})-[r:RATED]->(m) RETURN nodes(p) as nodes, rels(p) as rels",
                MapUtil.map("name", "Vince")).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();

        List<Object> nodes = (List) result.get("nodes");
        List<Object> rels = (List) result.get("rels");
        assertThat(nodes).hasSize(2);
        assertThat(rels).hasSize(1);

        for (Object o : nodes) {
            if (o instanceof User) {
                User user = (User) o;
                assertThat(user.getName()).isEqualTo("Vince");
                assertThat(user.getRatings()).hasSize(1);
                Movie movie = user.getRatings().iterator().next().getMovie();
                assertThat(movie).isNotNull();
                assertThat(movie.getTitle()).isEqualTo("Top Gear");
                Rating rating = movie.getRatings().iterator().next();
                assertThat(rating).isNotNull();
                assertThat(rating.getStars()).isEqualTo(4);
            }
        }
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapArrays() {
        Iterator<Map<String, Object>> results = session
            .query("MATCH (u:User {name:{name}}) RETURN u.array as arr", MapUtil.map("name", "Christophe")).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();
        assertThat(((String[]) result.get("arr")).length).isEqualTo(2);
    }

    @Test // DATAGRAPH-700
    public void shouldBeAbleToMapMixedArrays() {
        Iterator<Map<String, Object>> results = session
            .query("MATCH (u:User {name:{name}}) RETURN u.array as arr, [1,'two',true] as mixed",
                MapUtil.map("name", "Christophe")).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();
        assertThat(((String[]) result.get("arr")).length).isEqualTo(2);
        Object[] mixed = (Object[]) result.get("mixed");
        assertThat(mixed.length).isEqualTo(3);
        assertThat(((Number) mixed[0]).longValue()).isEqualTo(1L);
        assertThat(mixed[1]).isEqualTo("two");
        assertThat(mixed[2]).isEqualTo(true);
    }

    @Test // DATAGRAPH-700
    public void modifyingQueryShouldBeAbleToMapEntitiesAndReturnStatistics() {
        Result result = session
            .query("MATCH (u:User {name:{name}})-[:RATED]->(m) WITH u,m SET u.age={age} RETURN u as user, m as movie",
                MapUtil.map("name", "Vince", "age", 20));
        Iterator<Map<String, Object>> results = result.queryResults().iterator();
        assertThat(results).isNotNull();
        Map<String, Object> row = results.next();
        assertThat(row).isNotNull();
        User user = (User) row.get("user");
        assertThat(user).isNotNull();
        Movie movie = (Movie) row.get("movie");
        assertThat(movie).isNotNull();
        assertThat(user.getName()).isEqualTo("Vince");
        assertThat(movie.getTitle()).isEqualTo("Top Gear");
        assertThat(results.hasNext()).isFalse();
    }

    @Test // #136
    public void shouldNotOverflowIntegers() {
        long start = Integer.MAX_VALUE;
        session.query("CREATE (n:Sequence {id:{id}, next:{start}})", MapUtil.map("id", "test", "start", start));

        String incrementStmt = "MATCH (n:Sequence) WHERE n.id = {id} REMOVE n.lock SET n.next = n.next + {increment} RETURN n.next - {increment} as current";

        Result result = session.query(incrementStmt, MapUtil.map("id", "test", "increment", 1));
        assertThat(((Number) result.iterator().next().get("current")).longValue()).isEqualTo(start);

        result = session.query(incrementStmt, MapUtil.map("id", "test", "increment", 1));

        //expected:<2147483648> but was:<-2147483648>
        assertThat(((Number) result.iterator().next().get("current")).longValue()).isEqualTo(start + 1);
    }

    @Test // #150
    public void shouldLoadNodesWithUnmappedOrNoLabels() {
        int movieCount = 0;
        int userCount = 0;
        int unmappedCount = 0;
        int noLabelCount = 0;

        session.query("CREATE (unknown), (m:Unmapped), (n:Movie), (n)-[:UNKNOWN]->(m)", emptyMap());

        Result result = session.query("MATCH (n) return n", emptyMap());
        assertThat(result).isNotNull();

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
        assertThat(unmappedCount).isEqualTo(1);
        assertThat(noLabelCount).isEqualTo(1);
        assertThat(movieCount).isEqualTo(4);
        assertThat(userCount).isEqualTo(5);
    }

    @Test // GH-148
    public void shouldMapCypherCollectionsToArrays() {
        Iterator<Map<String, Object>> iterator = session
            .query("MATCH (n:User) return collect(n.name) as names", emptyMap()).iterator();
        assertThat(iterator.hasNext()).isTrue();
        Map<String, Object> row = iterator.next();
        assertThat(row.get("names").getClass().isArray()).isTrue();
        assertThat(((String[]) row.get("names")).length).isEqualTo(4);

        iterator = session
            .query("MATCH (n:User {name:'Michal'}) return collect(n.name) as names", emptyMap()).iterator();
        assertThat(iterator.hasNext()).isTrue();
        row = iterator.next();
        assertThat(row.get("names").getClass().isArray()).isTrue();
        assertThat(((String[]) row.get("names")).length).isEqualTo(1);

        iterator = session
            .query("MATCH (n:User {name:'Does Not Exist'}) return collect(n.name) as names", emptyMap())
            .iterator();
        assertThat(iterator.hasNext()).isTrue();
        row = iterator.next();
        assertThat(row.get("names").getClass().isArray()).isTrue();
        assertThat(((Object[]) row.get("names")).length).isEqualTo(0);
    }

    @Test
    public void shouldThrowExceptionIfTypeMismatchesInQueryForObject() {

        assertThatExceptionOfType(MappingException.class)
            .isThrownBy(() -> session.queryForObject(Restaurant.class, "MATCH (n:User) return count(n)", emptyMap()))
            .withMessage("Cannot map java.lang.Long to %s. This can be caused by missing registration of %1$s.",
                Restaurant.class.getName());
    }

    @Test
    public void queryForObjectFindsNestedClasses() {

        session.query("CREATE (:`NestingClass$Something`{name:'Test'})", emptyMap());

        NestingClass.Something something = session
            .queryForObject(NestingClass.Something.class, "MATCH (n:`NestingClass$Something`) return n", emptyMap());

        assertThat(something).isNotNull();
    }

    @Test // GH-496
    public void testQueryWithProjection() {
        Assume.assumeFalse(driver instanceof HttpDriver);

        Iterable<User> results = session
            .query(User.class,
                "MATCH (u:User) where u.name={name} return u "
                    + ",[[(u)-[r:EXTENDED_FRIEND]->(e) | [r, e   ]    ]  ]  ",
                Collections.singletonMap("name", "Vince"));
        assertThat(results).size().isEqualTo(1);
        User user = results.iterator().next();
        assertThat(user.getName()).isEqualTo("Vince");
        assertThat(user.getExtendedFriends()).isNotEmpty();
        assertThat(user.getExtendedFriends()).contains(new ExtendedUser(null, "extended", null));
    }

    @Test // GH-496
    public void testQueryWithExplicitRelationship() {
        Assume.assumeFalse(driver instanceof HttpDriver);

        Iterable<User> results = session
            .query(User.class,
                "MATCH (u:User) -[r:EXTENDED_FRIEND] ->(e)  where u.name={name} RETURN u, r, e",
                Collections.singletonMap("name", "Vince"));

        assertThat(results).extracting(User::getName).containsExactlyInAnyOrder("Vince", "extended");
    }

    @Test // GH-496
    public void shouldMaintainTheTraversalOrderFromMatchClause() {
        Assume.assumeFalse(driver instanceof HttpDriver);

        Iterable<Item> result = session
            .query(Item.class, "MATCH (i:Item)-[:NEXT*0..]->(n:Item) WHERE i.name={name} return n ,"
                    + "[ [ (n)-[r:BELONGS_TO]->(c:Item) | [r, c] ] ]",
                Collections.singletonMap("name", "A"));

        assertThat(result)
            .isNotNull()
            .extracting(Item::getName)
            .containsExactly("A", "B", "C", "D");
    }

    private static boolean checkForMichal(Map<String, Object> result) {
        if (result.get("n") instanceof User) {
            User u = (User) result.get("n");
            if (u.getName().equals("Michal")) {
                assertThat(u.getFriends()).hasSize(1);
                assertThat(u.getFriends().iterator().next().getName()).isEqualTo("Vince");
                return true;
            }
        }
        return false;
    }
}
