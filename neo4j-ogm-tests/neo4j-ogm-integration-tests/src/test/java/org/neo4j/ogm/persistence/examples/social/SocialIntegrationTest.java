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
package org.neo4j.ogm.persistence.examples.social;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.social.Individual;
import org.neo4j.ogm.domain.social.Mortal;
import org.neo4j.ogm.domain.social.Person;
import org.neo4j.ogm.domain.social.User;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 */
public class SocialIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.social");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @After
    public void clearDatabase() {
        session.purgeDatabase();
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void shouldFetchOnlyPeopleILike() {
        session.query("create (p1:Person {name:'A'}) create (p2:Person {name:'B'}) create (p3:Person {name:'C'})" +
                " create (p4:Person {name:'D'}) create (p1)-[:LIKES]->(p2) create (p1)-[:LIKES]->(p3) create (p4)-[:LIKES]->(p1)",
            Collections.EMPTY_MAP);

        Person personA = session.loadAll(Person.class, new Filter("name", ComparisonOperator.EQUALS, "A")).iterator()
            .next();
        assertThat(personA).isNotNull();
        assertThat(personA.getPeopleILike()).hasSize(2);

        Person personD = session.loadAll(Person.class, new Filter("name", ComparisonOperator.EQUALS, "D")).iterator()
            .next();
        assertThat(personD).isNotNull();
        assertThat(personD.getPeopleILike()).hasSize(1);
        assertThat(personD.getPeopleILike().get(0)).isEqualTo(personA);
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void shouldFetchFriendsInBothDirections() {
        session.query(
            "create (p1:Individual {name:'A'}) create (p2:Individual {name:'B'}) create (p3:Individual {name:'C'})" +
                " create (p4:Individual {name:'D'}) create (p1)-[:FRIENDS]->(p2) create (p1)-[:FRIENDS]->(p3) create (p4)-[:FRIENDS]->(p1)",
            Collections.EMPTY_MAP);

        Individual individualA = session.loadAll(Individual.class, new Filter("name", ComparisonOperator.EQUALS, "A"))
            .iterator().next();
        assertThat(individualA).isNotNull();
        assertThat(individualA.getFriends()).hasSize(2);
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void shouldFetchFriendsForUndirectedRelationship() {
        session.query("create (p1:User {name:'A'}) create (p2:User {name:'B'}) create (p3:User {name:'C'})" +
                " create (p4:User {name:'D'}) create (p1)-[:FRIEND]->(p2) create (p1)-[:FRIEND]->(p3) create (p4)-[:FRIEND]->(p1)",
            Collections.EMPTY_MAP);

        User userA = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "A")).iterator().next();
        assertThat(userA).isNotNull();
        assertThat(userA.getFriends()).hasSize(3);

        User userB = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "B")).iterator().next();
        assertThat(userB).isNotNull();
        assertThat(userB.getFriends()).hasSize(1);
        assertThat(userB.getFriends().get(0)).isEqualTo(userA);

        User userD = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "D")).iterator().next();
        assertThat(userD).isNotNull();
        assertThat(userD.getFriends()).hasSize(1);
        assertThat(userD.getFriends().get(0)).isEqualTo(userA);
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void shouldSaveUndirectedFriends() {
        User userA = new User("A");
        User userB = new User("B");
        User userC = new User("C");
        User userD = new User("D");

        userA.getFriends().add(userB);
        userA.getFriends().add(userC);
        userD.getFriends().add(userA);

        session.save(userA);
        session.save(userB);
        session.save(userC);
        session.save(userD);

        session.clear();

        userA = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "A")).iterator().next();
        assertThat(userA).isNotNull();
        assertThat(userA.getFriends()).hasSize(3);

        userB = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "B")).iterator().next();
        assertThat(userB).isNotNull();
        assertThat(userB.getFriends()).hasSize(1);
        assertThat(userB.getFriends().get(0).getName()).isEqualTo(userA.getName());

        userD = session.loadAll(User.class, new Filter("name", ComparisonOperator.EQUALS, "D")).iterator().next();
        assertThat(userD).isNotNull();
        assertThat(userD.getFriends()).hasSize(1);
        assertThat(userD.getFriends().get(0).getName()).isEqualTo(userA.getName());
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void shouldSaveUndirectedFriendsInBothDirections() {
        Person userA = new Person("A");
        Person userB = new Person("B");

        userA.getPeopleILike().add(userB);
        userB.getPeopleILike().add(userA);

        session.save(userA);

        session.clear();
        userA = session.loadAll(Person.class, new Filter("name", ComparisonOperator.EQUALS, "A")).iterator().next();
        assertThat(userA).isNotNull();
        assertThat(userA.getPeopleILike()).hasSize(1);
        session.clear();
        userB = session.loadAll(Person.class, new Filter("name", ComparisonOperator.EQUALS, "B")).iterator().next();
        assertThat(userB).isNotNull();
        assertThat(userB.getPeopleILike()).hasSize(1);
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void shouldSaveIncomingKnownMortals() {
        Mortal mortalA = new Mortal("A");
        Mortal mortalB = new Mortal("B");
        Mortal mortalC = new Mortal("C");
        Mortal mortalD = new Mortal("D");

        mortalA.getKnownBy().add(mortalB);
        mortalA.getKnownBy().add(mortalC);
        mortalD.getKnownBy().add(mortalA);

        session.save(mortalA);
        session.save(mortalB);
        session.save(mortalC);
        session.save(mortalD);

        session.clear();

        mortalA = session.loadAll(Mortal.class, new Filter("name", ComparisonOperator.EQUALS, "A")).iterator().next();
        assertThat(mortalA).isNotNull();
        assertThat(mortalA.getKnownBy()).hasSize(2);

        mortalB = session.loadAll(Mortal.class, new Filter("name", ComparisonOperator.EQUALS, "B")).iterator().next();
        assertThat(mortalB).isNotNull();
        assertThat(mortalB.getKnownBy()).isEmpty();

        mortalC = session.loadAll(Mortal.class, new Filter("name", ComparisonOperator.EQUALS, "C")).iterator().next();
        assertThat(mortalC).isNotNull();
        assertThat(mortalC.getKnownBy()).isEmpty();

        mortalD = session.loadAll(Mortal.class, new Filter("name", ComparisonOperator.EQUALS, "D")).iterator().next();
        assertThat(mortalD).isNotNull();
        assertThat(mortalD.getKnownBy()).hasSize(1);
        assertThat(mortalD.getKnownBy().iterator().next().getName()).isEqualTo("A");
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void shouldFetchIncomingKnownMortals() {
        session.query("create (m1:Mortal {name:'A'}) create (m2:Mortal {name:'B'}) create (m3:Mortal {name:'C'})" +
                " create (m4:Mortal {name:'D'}) create (m1)<-[:KNOWN_BY]-(m2) create (m1)<-[:KNOWN_BY]-(m3) create (m4)<-[:KNOWN_BY]-(m1)",
            Collections.EMPTY_MAP);

        Mortal mortalA = session.loadAll(Mortal.class, new Filter("name", ComparisonOperator.EQUALS, "A")).iterator()
            .next();
        assertThat(mortalA).isNotNull();
        assertThat(mortalA.getKnownBy()).hasSize(2);

        Mortal mortalB = session.loadAll(Mortal.class, new Filter("name", ComparisonOperator.EQUALS, "B")).iterator()
            .next();
        assertThat(mortalB).isNotNull();
        assertThat(mortalB.getKnownBy()).isEmpty();

        Mortal mortalC = session.loadAll(Mortal.class, new Filter("name", ComparisonOperator.EQUALS, "C")).iterator()
            .next();
        assertThat(mortalC).isNotNull();
        assertThat(mortalC.getKnownBy()).isEmpty();

        Mortal mortalD = session.loadAll(Mortal.class, new Filter("name", ComparisonOperator.EQUALS, "D")).iterator()
            .next();
        assertThat(mortalD).isNotNull();
        assertThat(mortalD.getKnownBy()).hasSize(1);
        assertThat(mortalD.getKnownBy().iterator().next().getName()).isEqualTo("A");
    }

    @Test
    public void shouldFetchFriendsUndirected() {

        User adam = new User("Adam");
        User daniela = new User("Daniela");
        User michal = new User("Michal");
        User vince = new User("Vince");

        adam.befriend(daniela);
        daniela.befriend(michal);
        michal.befriend(vince);

        session.save(adam);

        session.clear();
        adam = session.load(User.class, adam.getId());
        assertThat(adam.getFriends()).hasSize(1);

        daniela = session.load(User.class, daniela.getId());
        assertThat(daniela.getFriends()).hasSize(2);
        List<String> friendNames = new ArrayList<>();
        for (User friend : daniela.getFriends()) {
            friendNames.add(friend.getName());
        }
        assertThat(friendNames.contains("Adam")).isTrue();
        assertThat(friendNames.contains("Michal")).isTrue();

        session.clear();

        michal = session.load(User.class, michal.getId());
        assertThat(michal.getFriends()).hasSize(2);

        session.clear();
        vince = session.load(User.class, vince.getId());
        assertThat(vince.getFriends()).hasSize(1);
    }

    /**
     * Issue #407
     * Should create graph like this
     * (a)-[:LIKES]->(b)-[:LIKES]->(c)
     * (a)-[:LIKES]->(d)-[:LIKES]->(e)
     * (b)-[:LIKES]->(d)
     * (d)-[:LIKES]->(c)
     * Issue was that the logic reaches either b (or d) with horizon 0 (2 steps from a)
     * and doesn't continue to save c (or e)
     */
    @Test
    public void shouldSaveObjectsToCorrectDepth() throws Exception {

        Person a = new Person("A");
        Person b = new Person("B");
        Person c = new Person("C");
        Person d = new Person("D");
        Person e = new Person("E");

        a.addPersonILike(b);
        b.addPersonILike(c);

        a.addPersonILike(d);
        d.addPersonILike(e);

        b.addPersonILike(d);
        d.addPersonILike(b);

        session.save(a, 2);

        session.clear();
        Collection<Person> people = session.loadAll(Person.class);
        assertThat(people).hasSize(5);
    }

    @Test
    public void shouldSaveAllDirectedRelationships() throws Exception {

        Person a = new Person("A");
        Person b = new Person("B");
        Person c = new Person("C");
        Person d = new Person("D");
        Person e = new Person("E");

        a.addPersonILike(b);
        b.addPersonILike(c);

        a.addPersonILike(d);
        d.addPersonILike(e);

        b.addPersonILike(d);
        d.addPersonILike(b);

        session.save(a);
        session.clear();

        Person loadedB = session.load(Person.class, b.getId());
        assertThat(loadedB.getPeopleILike()).hasSize(2);

        Person loadedD = session.load(Person.class, d.getId());
        assertThat(loadedD.getPeopleILike()).hasSize(2);
    }

}
