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
package org.neo4j.ogm.context;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Condition;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.minimum.Actor;
import org.neo4j.ogm.domain.cineasts.minimum.Movie;
import org.neo4j.ogm.domain.cineasts.minimum.Role;
import org.neo4j.ogm.domain.cineasts.minimum.SomeQueryResult;
import org.neo4j.ogm.domain.gh391.SomeContainer;
import org.neo4j.ogm.domain.gh551.AnotherThing;
import org.neo4j.ogm.domain.gh551.ThingEntity;
import org.neo4j.ogm.domain.gh551.ThingResult;
import org.neo4j.ogm.domain.gh551.ThingResult2;
import org.neo4j.ogm.domain.gh551.ThingWIthId;
import org.neo4j.ogm.domain.gh552.Thing;
import org.neo4j.ogm.domain.gh750.ThingResult3;
import org.neo4j.ogm.domain.gh750.ThingResult4;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.reflect.ReflectionEntityInstantiator;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.LoggerRule;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class SingleUseEntityMapperTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    @Rule
    public final LoggerRule loggerRule = new LoggerRule();

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.gh551", "org.neo4j.ogm.domain.gh391",
            "org.neo4j.ogm.domain.gh750", "org.neo4j.ogm.domain.cineasts.minimum");

        // Prepare test data
        Session session = sessionFactory.openSession();
        session.query("MATCH (n) DETACH DELETE n", EMPTY_MAP);
        session.query("unwind range(1,10) as x with x create (n:ThingEntity {name: 'Thing ' + x}) return n", EMPTY_MAP);

        Actor actor = new Actor("A1");
        Movie movie = new Movie("M1");
        Role role = new Role("R1", actor, movie);
        session.save(role);

        movie = new Movie("M2");
        role = new Role("R2", actor, movie);
        session.save(role);
    }

    @Test // GH-551
    public void singleUseEntityMapperShouldWorkWithNestedObjects() {

        SingleUseEntityMapper entityMapper =
            new SingleUseEntityMapper(sessionFactory.metaData(),
                new ReflectionEntityInstantiator(sessionFactory.metaData()));

        Iterable<Map<String, Object>> results = sessionFactory.openSession()
            .query("MATCH (t:ThingEntity) RETURN 'a name' as something, collect({name: t.name}) as things", EMPTY_MAP)
            .queryResults();

        assertThat(results).hasSize(1);

        ThingResult thingResult = entityMapper.map(ThingResult.class, results.iterator().next());
        assertThat(thingResult.getSomething()).isEqualTo("a name");
        assertThat(thingResult.getThings())
            .hasSize(10)
            .extracting(AnotherThing::getName)
            .allSatisfy(s -> s.startsWith("Thing"));
    }

    @Test // GH-748
    public void singleUseEntityMapperShouldWorkWithNullableNestedNodeEntities() {

        SingleUseEntityMapper entityMapper =
            new SingleUseEntityMapper(sessionFactory.metaData(),
                new ReflectionEntityInstantiator(sessionFactory.metaData()));

        Iterable<Map<String, Object>> results = sessionFactory.openSession()
            .query("WITH 'a name' AS something OPTIONAL MATCH (t:ThingEntity {na:false}) RETURN something, t as entity",
                EMPTY_MAP)
            .queryResults();

        assertThat(results).hasSize(1);

        ThingResult2 thingResult = entityMapper.map(ThingResult2.class, results.iterator().next());
        assertThat(thingResult.getSomething()).isEqualTo("a name");
        assertThat(thingResult.getEntity()).isNull();
    }

    @Test // GH-748
    public void singleUseEntityMapperShouldWorkWithNonNullNestedNodeEntities() {

        SingleUseEntityMapper entityMapper =
            new SingleUseEntityMapper(sessionFactory.metaData(),
                new ReflectionEntityInstantiator(sessionFactory.metaData()));

        Iterable<Map<String, Object>> results = sessionFactory.openSession()
            .query(
                "WITH 'a name' AS something OPTIONAL MATCH (t:ThingEntity {name: 'Thing 7'}) RETURN something, t as entity",
                EMPTY_MAP)
            .queryResults();

        assertThat(results).hasSize(1);

        ThingResult2 thingResult = entityMapper.map(ThingResult2.class, results.iterator().next());
        assertThat(thingResult.getSomething()).isEqualTo("a name");
        assertThat(thingResult.getEntity()).isNotNull().extracting(ThingEntity::getName).isEqualTo("Thing 7");
    }

    @Test // GH-748
    public void shouldFailOnIncompatibleProperties() {

        SingleUseEntityMapper entityMapper =
            new SingleUseEntityMapper(sessionFactory.metaData(),
                new ReflectionEntityInstantiator(sessionFactory.metaData()));

        Iterable<Map<String, Object>> results = sessionFactory.openSession()
            .query("WITH 'a name' AS something OPTIONAL MATCH (t:ThingEntity) RETURN something, COLLECT(t) as entity",
                EMPTY_MAP)
            .queryResults();

        assertThat(results).hasSize(1);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> entityMapper.map(ThingResult2.class, results.iterator().next()))
            .withMessageContaining(
                "Can not set org.neo4j.ogm.domain.gh551.ThingEntity field org.neo4j.ogm.domain.gh551.ThingResult2.entity to java.util.ArrayList");
        Condition<String> stringMatches = new Condition<>(s -> s.contains(
            "Cannot map property entity from result set: The result contains more than one entry for the property."),
            "String matches");
        assertThat(loggerRule.getFormattedMessages()).areAtLeastOne(stringMatches);
    }

    @Test // GH-748
    public void shouldBeLenientWithSingleValuedCollectionsForSkalarPropertiesMode() {

        SingleUseEntityMapper entityMapper =
            new SingleUseEntityMapper(sessionFactory.metaData(),
                new ReflectionEntityInstantiator(sessionFactory.metaData()));

        Iterable<Map<String, Object>> results = sessionFactory.openSession()
            .query(
                "WITH 'a name' AS something OPTIONAL MATCH (t:ThingEntity {name: 'Thing 7'}) RETURN something, COLLECT(t) as entity",
                EMPTY_MAP)
            .queryResults();

        assertThat(results).hasSize(1);

        ThingResult2 thingResult = entityMapper.map(ThingResult2.class, results.iterator().next());
        assertThat(thingResult.getSomething()).isEqualTo("a name");
        assertThat(thingResult.getEntity()).isNotNull().extracting(ThingEntity::getName).isEqualTo("Thing 7");
    }

    @Test // GH-750
    public void shouldWorkWithCustomConverters() {

        SingleUseEntityMapper entityMapper =
            new SingleUseEntityMapper(sessionFactory.metaData(),
                new ReflectionEntityInstantiator(sessionFactory.metaData()));

        Iterable<Map<String, Object>> results = sessionFactory.openSession()
            .query("RETURN 'foo' AS foobar", EMPTY_MAP)
            .queryResults();

        assertThat(results).hasSize(1);

        ThingResult3 thingResult = entityMapper.map(ThingResult3.class, results.iterator().next());
        assertThat(thingResult.getFoobar()).isNotNull();
        assertThat(thingResult.getFoobar().getValue()).isEqualTo("foo");
    }

    @Test // GH-750
    public void shouldWorkWithCustomConvertersOnListProperty() {
        SingleUseEntityMapper entityMapper =
            new SingleUseEntityMapper(sessionFactory.metaData(),
                new ReflectionEntityInstantiator(sessionFactory.metaData()));
        Iterable<Map<String, Object>> results = sessionFactory.openSession()
            .query("RETURN ['foo', 'bar'] AS foobar", EMPTY_MAP)
            .queryResults();
        assertThat(results).hasSize(1);
        ThingResult4 thingResult = entityMapper.map(ThingResult4.class, results.iterator().next());
        assertThat(thingResult.getFoobar()).hasSize(2);
        assertThat(thingResult.getFoobar().get(0).getValue()).isEqualTo("foo");
        assertThat(thingResult.getFoobar().get(1).getValue()).isEqualTo("bar");
    }

    @Test // GH-718
    public void queryResultShouldHandleNodeAndRelationshipEntities() {

        SingleUseEntityMapper entityMapper =
            new SingleUseEntityMapper(sessionFactory.metaData(),
                new ReflectionEntityInstantiator(sessionFactory.metaData()));

        // Notice the difference in how the relationship is queried: The variable length query pattern in
        // the second query triggers the changes that had been necessary in org.neo4j.ogm.result.adapter.RestModelAdapter.adapt
        // so that it works the same way as the org.neo4j.ogm.result.adapter.GraphModelAdapter.adapt
        for (String query : new String[] {
            "MATCH (a:Actor {name: 'A1'})-[r:ACTS_IN]->(m:Movie) RETURN a AS actor,COLLECT(r) AS roles, COLLECT(m) as movies",
            "MATCH (a:Actor {name: 'A1'})-[r:ACTS_IN*]->(m:Movie) RETURN a AS actor,COLLECT(r) AS roles, COLLECT(m) as movies"
        }) {
            Iterable<Map<String, Object>> results = sessionFactory.openSession().query(query, EMPTY_MAP)
                .queryResults();

            assertThat(results).hasSize(1);
            SomeQueryResult thingResult = entityMapper.map(SomeQueryResult.class, results.iterator().next());
            assertThat(thingResult.getActor().getName()).isEqualTo("A1");
            assertThat(thingResult.getRoles())
                .hasSize(2)
                .extracting(Role::getPlayed)
                .containsExactlyInAnyOrder("R1", "R2");
        }
    }

    /**
     * ID fields are treated differently in different versions of OGM. This tests assures that they work correctly.
     */
    @Test // GH-551
    public void shouldUseIdFields() {

        SingleUseEntityMapper entityMapper =
            new SingleUseEntityMapper(sessionFactory.metaData(),
                new ReflectionEntityInstantiator(sessionFactory.metaData()));

        Iterable<Map<String, Object>> results = sessionFactory.openSession()
            .query("MATCH (t:ThingEntity) RETURN 4711 as id, 'a name' as name LIMIT 1", EMPTY_MAP)
            .queryResults();

        assertThat(results).hasSize(1);

        ThingWIthId thingResult = entityMapper.map(ThingWIthId.class, results.iterator().next());
        assertThat(thingResult.getName()).isEqualTo("a name");
        assertThat(thingResult.getId()).isEqualTo(4711);
    }

    @Test // GH-552
    public void shouldLookupCorrectRootClass() {
        MetaData metaData = new MetaData("org.neo4j.ogm.domain.gh552");
        String propertyKey = "notAName";
        Map<String, Object> properties = Collections.singletonMap(propertyKey, "NOT A NAME!!!");

        SingleUseEntityMapper entityMapper = new SingleUseEntityMapper(metaData,
            new ReflectionEntityInstantiator(metaData));
        Thing thing = entityMapper.map(Thing.class, properties);
        assertThat(thing.getNotAName()).isEqualTo(properties.get(propertyKey));
    }

    @Test // GH-391
    public void shouldDealWithStaticInnerClasses() {

        SingleUseEntityMapper entityMapper =
            new SingleUseEntityMapper(sessionFactory.metaData(),
                new ReflectionEntityInstantiator(sessionFactory.metaData()));

        Iterable<Map<String, Object>> results = sessionFactory.openSession()
            .query("MATCH (t:ThingEntity) RETURN 'a name' as something LIMIT 1", EMPTY_MAP)
            .queryResults();

        assertThat(results).hasSize(1);

        SomeContainer.StaticInnerThingResult thingResult = entityMapper.map(SomeContainer.StaticInnerThingResult.class, results.iterator().next());
        assertThat(thingResult.getSomething()).isEqualTo("a name");
    }

    @Test
    public void shouldMapFromMap() {

        MetaData metaData = new MetaData("org.neo4j.ogm.context");
        SingleUseEntityMapper entityMapper = new SingleUseEntityMapper(metaData,
            new ReflectionEntityInstantiator(metaData));

        Collection<Object> toReturn = new ArrayList<>();

        Iterable<Map<String, Object>> results = getQueryResults();
        for (Map<String, Object> result : results) {
            toReturn.add(entityMapper.map(UserResult.class, result));
        }

        assertThat(toReturn).hasSize(1);
        assertThat(toReturn).first().isInstanceOf(UserResult.class);
        UserResult userResult = (UserResult) toReturn.iterator().next();
        assertThat(userResult.getProfile()).containsAllEntriesOf(
            (Map<? extends String, ?>) results.iterator().next().get("profile"));
    }

    private Iterable<Map<String, Object>> getQueryResults() {

        Map<String, Object> profile = new HashMap<>();
        profile.put("enterpriseId", "p.enterpriseId");
        profile.put("clidUuid", "u.clidUuid");
        profile.put("profileId", "p.clidUuid");
        profile.put("firstName", "p.firstName");
        profile.put("lastName", "p.lastName");
        profile.put("email", "u.email");
        profile.put("roles", "roles");
        profile.put("connectionType", "connectionType");

        return Collections.singletonList(Collections.singletonMap("profile", profile));
    }
}
