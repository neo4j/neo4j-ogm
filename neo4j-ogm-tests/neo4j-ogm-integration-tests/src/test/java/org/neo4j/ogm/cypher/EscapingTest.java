/*
 * Copyright (c) 2002-2026 "Neo4j,"
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
package org.neo4j.ogm.cypher;

import java.io.IOException;
import java.util.Collection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.cypher.function.DistanceFromNativePoint;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.domain.escaping.Article;
import org.neo4j.ogm.domain.escaping.Person;
import org.neo4j.ogm.domain.escaping.Place;
import org.neo4j.ogm.domain.escaping.SomeModel;
import org.neo4j.ogm.domain.escaping.Thing;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.types.spatial.CartesianPoint2d;

import static org.assertj.core.api.Assertions.*;
import static org.neo4j.ogm.cypher.function.NativeDistanceComparison.*;

public class EscapingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    private static org.neo4j.driver.Driver nativeDriver;


    @BeforeAll
    public static void oneTimeSetUp() {

        Configuration ogmConfiguration = getBaseConfigurationBuilder()
            .useNativeTypes()
            .build();

        Driver driver = new BoltDriver();
        driver.configure(ogmConfiguration);

        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.escaping");
        nativeDriver = driver.unwrap(org.neo4j.driver.Driver.class);
    }


    @BeforeEach
    public void init() throws IOException {

        try(var localSession = nativeDriver.session()) {
            localSession.run("CREATE (p:Person {name: 'testperson1', `name`` IS NOT NULL WITH n MERGE (u:User) RETURN u.password AS pw //`: 'anything'})").consume();
            localSession.run("CREATE (p:Person {name: 'testperson2', `name`` DESC WITH n MERGE (u:User) RETURN u.password AS pw //`: '1'})").consume();
            localSession.run("CREATE (p:Person {name: 'testperson3', `name`` DESC WITH n MERGE (u:User) RETURN u.password AS pw //`: '2'})").consume();
            localSession.run("CREATE (p:Place {name: 'testplace', `l) < $distanceValue WITH n MERGE (u:User) RETURN u.password AS pw //`:  point({x: 1, y: 1, srid: 7203})})").consume();
        }
        session = sessionFactory.openSession();

    }

    @AfterEach
    public void clear() {
        session.purgeDatabase();
    }

    @Test // F3
    void dynamicLabelsShouldPreventInjection() {
        var a = new Article();
        a.setBody("whatever");
        var tag = "x`) WITH n MERGE (u:User) SET u.role = 'ADMIN' //";
        a.addTag(tag);
        session.save(a);

        assertNoUserHasBeenCreated();

        a = session.load(Article.class, a.getId());
        assertThat(a.getTags()).containsExactly(tag);
    }

    private static void assertNoUserHasBeenCreated() {
        try (var localSession = nativeDriver.session()) {
            var result = localSession.run("MATCH (u:User) RETURN count(u)").single().get(0).asLong();
            assertThat(result).isZero();
        }
    }

    @Test // F1
    void searchFilterShouldPreventInjection() {

        var field = "name` IS NOT NULL WITH n MERGE (u:User) RETURN u.password AS pw //";
        var filter = new Filter(field, ComparisonOperator.EQUALS, "anything");

        session.clear();
        var people = session.loadAll(Person.class, new Filters(filter));

        assertNoUserHasBeenCreated();
        assertThat(people)
            .hasSize(1)
            .first().extracting(Person::getName).isEqualTo("testperson1");
    }

    @Test // F2
    void sortFieldsShouldPreventInjection() {

        var sortParam = "name` DESC WITH n MERGE (u:User) RETURN u.password AS pw //";
        var order = new SortOrder().asc(sortParam);

        session.clear();
        var people = session.loadAll(Person.class, new Filters(), order, new Pagination(0, 20), 1);

        assertNoUserHasBeenCreated();
        assertThat(people)
            .hasSize(3)
            .first().extracting(Person::getName).isEqualTo("testperson2");
    }

    @Test // F5
    void locationFilterShouldPreventInjection() {

        var userField = "l) < $distanceValue WITH n MERGE (u:User) RETURN u.password AS pw //";
        var filter = new Filter(userField, distanceComparisonFor(new DistanceFromNativePoint(new CartesianPoint2d(0, 0), 2)));

        session.clear();
        var places = session.loadAll(Place.class, new Filters(filter));

        assertNoUserHasBeenCreated();
        assertThat(places)
            .hasSize(1)
            .first().extracting(Place::getName).isEqualTo("testplace");
    }

    @Test // F4
    void customPropertiesShouldPreventInjection() {

        var t = new Thing();
        var key = "x` WITH n MERGE (u:User) //";
        t.addAttr(key, "v");
        session.save(t);

        assertNoUserHasBeenCreated();

        t.removeAttr(key);
        session.save(t);

        session.clear();
        t = session.load(Thing.class, t.getId());

        assertNoUserHasBeenCreated();
        assertThat(t.getAttrs()).doesNotContainKey(key);
    }

    @Test // F11
    void shouldSaveEntitiesWithUnusualIds() {

        var someModel = new SomeModel();
        someModel.setBusinessKey("my key");
        session.save(someModel);

        session.clear();
        someModel = session.load(SomeModel.class, "my key");
        assertThat(someModel).isNotNull();
    }
}
