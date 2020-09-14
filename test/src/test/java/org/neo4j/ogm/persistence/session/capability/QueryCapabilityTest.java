/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

import static org.assertj.core.api.Assertions.*;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.neo4j.helpers.collection.MapUtil;
import org.mockito.Mockito;
import org.neo4j.ogm.context.MappingContext;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Rating;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.domain.gh726.package_a.SameClass;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.testutil.TestUtils;
import org.slf4j.LoggerFactory;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class QueryCapabilityTest extends MultiDriverTestClass {

    private Session session;

    @Rule
    public final LoggerRule loggerRule = new LoggerRule();

    @Before
    public void init() throws IOException {
        session = new SessionFactory(driver,
            "org.neo4j.ogm.domain.cineasts.annotated",
            "org.neo4j.ogm.domain.gh726")
            .openSession();
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

    @Test // DATAGRAPH-697
    public void readOnlyQueryMustBeReadOnly() {

        session.save(new Actor("Jeff"));
        session.query("MATCH (a:Actor) SET a.age={age}", MapUtil.map("age", 5), true);
        Condition<String> stringMatches = new Condition<>(s -> s.contains(
            "Cypher query contains keywords that indicate a writing query but OGM is going to use a read only transaction as requested, so the query might fail."),
            "String matches");
        assertThat(loggerRule.getFormattedMessages()).areAtLeastOne(stringMatches);
    }

    @Test
    public void shouldBeAbleToIndicateSafeCall() throws NoSuchFieldException, IllegalAccessException {

        // Don' think too long about that bloody mess…
        MappingContext spyOnMappingContext = Mockito.spy(((Neo4jSession) session).context());
        Field mappingContextField = Neo4jSession.class.getDeclaredField("mappingContext");
        mappingContextField.setAccessible(true);
        mappingContextField.set(session, spyOnMappingContext);

        Iterable<String> results = session
            .query(String.class, "CALL dbms.procedures() yield name", Collections.emptyMap());
        assertThat(results).isNotEmpty();
        results = session
            .query(String.class, "CALL dbms.procedures() yield name /*+ OGM READ_ONLY */", Collections.emptyMap());
        assertThat(results).isNotEmpty();
        results = session
            .query(String.class, "CALL dbms.procedures() yield name \n/*+ OGM READ_ONLY */", Collections.emptyMap());
        assertThat(results).isNotEmpty();
        results = session
            .query(String.class, "CALL /*+ OGM READ_ONLY */ dbms.procedures() yield name", Collections.emptyMap());
        assertThat(results).isNotEmpty();

        Mockito.verify(spyOnMappingContext, Mockito.atMost(1)).clear();
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
            assertThat(((Number) row.get("a.age")).longValue()).isEqualTo(5l);
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
        Result result = session.query("MATCH (a:Actor) RETURN a.name", Collections.EMPTY_MAP, true);
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
            .query("MATCH (a:Actor) return a.nonExistent as nonExistent", Collections.EMPTY_MAP);
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
                Collections.EMPTY_MAP).iterator();
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
                Collections.EMPTY_MAP).iterator();
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

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapRelationshipEntities() {
        Iterator<Map<String, Object>> results = session
            .query("MATCH (u:User {name:$name})-[r:RATED]->(m) RETURN u,r,m", Collections.singletonMap("name", "Vince"))
            .iterator();
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

    @Test // GH-651
    public void shouldBeAbleToMapRelationshipEntitiesByIds() {
        List<Long> ratingIds = new ArrayList<>();
        for (Map<String, Object> row : session
            .query("MATCH ()-[r:RATED]->() RETURN id(r) as r", Collections.emptyMap())
            .queryResults()) {
            ratingIds.add((Long) row.get("r"));
        }

        Collection<Rating> ratings = session.loadAll(Rating.class, ratingIds);
        assertThat(ratings).extracting(Rating::getId).containsAll(ratingIds);
    }

    /**
     * @see DATAGRAPH-700
     */
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

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapVariableDepthRelationshipsWithCompletePaths() {
        Iterator<Map<String, Object>> results = session
            .query("match (u:User {name:{name}}) match (u)-[r*0..1]-(n) return u,r,n", MapUtil.map("name", "Vince"))
            .iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();
        boolean foundMichal = false;
        /*
            Expect 3 rows:
             one with (vince)-[:FRIENDS]-(michal)
             one with (vince)-[:RATED]-(top gear)
             one with (vince)
         */

        User user = (User) result.get("u");
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo("Vince");
        foundMichal = checkForMichal(result, foundMichal);

        result = results.next();
        assertThat(result).isNotNull();
        foundMichal = checkForMichal(result, foundMichal);

        result = results.next();
        assertThat(result).isNotNull();
        foundMichal = checkForMichal(result, foundMichal);

        assertThat(foundMichal).isTrue();
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
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

    /**
     * @see DATAGRAPH-700
     */
    @Test
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

    /**
     * @see DATAGRAPH-700
     */
    @Test
    public void shouldBeAbleToMapArrays() {
        Iterator<Map<String, Object>> results = session
            .query("MATCH (u:User {name:{name}}) RETURN u.array as arr", MapUtil.map("name", "Christophe")).iterator();
        assertThat(results).isNotNull();
        Map<String, Object> result = results.next();
        assertThat(result).isNotNull();
        assertThat(((String[]) result.get("arr")).length).isEqualTo(2);
    }

    /**
     * @see DATAGRAPH-700
     */
    @Test
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

    /**
     * @see DATAGRAPH-700
     */
    @Test
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

    /**
     * @see Issue 136
     */
    @Test
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

    /**
     * @see Issue 150
     */
    @Test
    public void shouldLoadNodesWithUnmappedOrNoLabels() {
        int movieCount = 0, userCount = 0, unmappedCount = 0, noLabelCount = 0;

        session.query("CREATE (unknown), (m:Unmapped), (n:Movie), (n)-[:UNKNOWN]->(m)", Collections.EMPTY_MAP);

        Result result = session.query("MATCH (n) return n", Collections.EMPTY_MAP);
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
        assertThat(userCount).isEqualTo(4);
    }

    /**
     * @see Issue 148
     */
    @Test
    public void shouldMapCypherCollectionsToArrays() {
        Iterator<Map<String, Object>> iterator = session
            .query("MATCH (n:User) return collect(n.name) as names", Collections.EMPTY_MAP).iterator();
        assertThat(iterator.hasNext()).isTrue();
        Map<String, Object> row = iterator.next();
        assertThat(row.get("names").getClass().isArray()).isTrue();
        assertThat(((String[]) row.get("names")).length).isEqualTo(4);

        iterator = session
            .query("MATCH (n:User {name:'Michal'}) return collect(n.name) as names", Collections.EMPTY_MAP).iterator();
        assertThat(iterator.hasNext()).isTrue();
        row = iterator.next();
        assertThat(row.get("names").getClass().isArray()).isTrue();
        assertThat(((String[]) row.get("names")).length).isEqualTo(1);

        iterator = session
            .query("MATCH (n:User {name:'Does Not Exist'}) return collect(n.name) as names", Collections.EMPTY_MAP)
            .iterator();
        assertThat(iterator.hasNext()).isTrue();
        row = iterator.next();
        assertThat(row.get("names").getClass().isArray()).isTrue();
        assertThat(((Object[]) row.get("names")).length).isEqualTo(0);
    }

    @Test // GH-671
    public void shouldNotThrowExceptionIfTypeIsSuperTypeOfResultObject() {
        session.queryForObject(Long.class, "MATCH (n:User) return count(n)", Collections.EMPTY_MAP);
        session.queryForObject(Number.class, "MATCH (n:User) return count(n)", Collections.EMPTY_MAP);
    }

    @Test // GH-726
    public void shouldMapCorrectlyIfTwoClassesWithTheSameSimpleNameExist() {
        // org.neo4j.ogm.domain.gh726.package_a.SameClass
        SameClass sameClassA = new SameClass();
        session.save(sameClassA);

        SameClass loadedSameClassA = session.query(SameClass.class,
            "MATCH (s:SameClassA) WHERE id(s) = $id RETURN s",
            Collections.singletonMap("id", sameClassA.getId())).iterator().next();

        assertThat(loadedSameClassA).isInstanceOf(SameClass.class);

        // org.neo4j.ogm.domain.gh726.package_b.SameClass
        org.neo4j.ogm.domain.gh726.package_b.SameClass sameClassB = new org.neo4j.ogm.domain.gh726.package_b.SameClass();
        session.save(sameClassB);

        org.neo4j.ogm.domain.gh726.package_b.SameClass loadedSameClassB =
            session.query(org.neo4j.ogm.domain.gh726.package_b.SameClass.class,
                "MATCH (s:SameClassB) WHERE id(s) = $id RETURN s",
                Collections.singletonMap("id", sameClassB.getId())).iterator().next();

        assertThat(loadedSameClassB).isInstanceOf(org.neo4j.ogm.domain.gh726.package_b.SameClass.class);
    }

    private boolean checkForMichal(Map<String, Object> result, boolean foundMichal) {
        if (result.get("n") instanceof User) {
            User u = (User) result.get("n");
            if (u.getName().equals("Michal")) {
                assertThat(u.getFriends()).hasSize(1);
                assertThat(u.getFriends().iterator().next().getName()).isEqualTo("Vince");
                foundMichal = true;
            }
        }
        return foundMichal;
    }

    static class LoggerRule implements TestRule {

        private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        private final Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        @Override
        public Statement apply(Statement base, Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    setup();
                    base.evaluate();
                    teardown();
                }
            };
        }

        private void setup() {
            logger.addAppender(listAppender);
            listAppender.start();
        }

        private void teardown() {
            listAppender.stop();
            listAppender.list.clear();
            logger.detachAppender(listAppender);
        }

        public List<String> getMessages() {
            return listAppender.list.stream().map(e -> e.getMessage()).collect(Collectors.toList());
        }

        public List<String> getFormattedMessages() {
            return listAppender.list.stream().map(e -> e.getFormattedMessage()).collect(Collectors.toList());
        }

    }
}
