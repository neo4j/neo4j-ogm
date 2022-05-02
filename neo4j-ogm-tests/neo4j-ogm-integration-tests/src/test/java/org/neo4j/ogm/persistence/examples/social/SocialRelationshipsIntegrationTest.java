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

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.social.Individual;
import org.neo4j.ogm.domain.social.Mortal;
import org.neo4j.ogm.domain.social.Person;
import org.neo4j.ogm.domain.social.SocialUser;
import org.neo4j.ogm.domain.social.User;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.event.EventListenerAdapter;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class SocialRelationshipsIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.social");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @After
    public void clearDatabase() {
        session.purgeDatabase();
    }

    @Test // DATAGRAPH-594
    public void saveUndirectedSavesOutgoingRelationship() {
        User userA = new User("A");
        User userB = new User("B");
        userA.getFriends().add(userB);
        session.save(userA);

        session.clear();
        assertThat(session.query("MATCH (a:User {name:'A'}) -[:FRIEND]-> (b:User {name:'B'}) RETURN a, b", emptyMap()).queryResults()).hasSize(1);
    }

    @Test // DATAGRAPH-594
    public void saveUnmarkedSavesOutgoingRelationship() {
        Individual individualA = new Individual();
        individualA.setName("A");
        Individual individualB = new Individual();
        individualB.setName("B");
        individualA.setFriends(Collections.singletonList(individualB));
        session.save(individualA);

        session.clear();
        assertThat(
            session.query("MATCH (a:Individual {name:'A', age: 0, code:0, bankBalance:0.0}) -[:FRIENDS]->"
                + "(b:Individual {name:'B', age:0, code:0, bankBalance:0.0}) RETURN a, b", emptyMap())
                .queryResults())
            .hasSize(1);
    }

    @Test // DATAGRAPH-594
    public void saveOutgoingSavesOutgoingRelationship() {
        Person personA = new Person("A");
        Person personB = new Person("B");
        personA.getPeopleILike().add(personB);
        session.save(personA);

        session.clear();
        assertThat(session.query("MATCH (a:Person {name:'A'}) -[:LIKES]-> (b:Person {name:'B'}) RETURN a, b", emptyMap()).queryResults()).hasSize(1);
    }

    @Test // DATAGRAPH-594
    public void saveIncomingSavesIncomingRelationship() {
        Mortal mortalA = new Mortal("A");
        Mortal mortalB = new Mortal("B");
        mortalA.getKnownBy().add(mortalB);
        session.save(mortalA);

        session.clear();
        assertThat(session.query("MATCH (a:Mortal {name:'A'}) <-[:KNOWN_BY]- (b:Mortal {name:'B'}) RETURN a, b", emptyMap()).queryResults()).hasSize(1);
    }

    @Test // DATAGRAPH-594
    public void saveOutgoingSavesOutgoingRelationshipInBothDirections() {
        Person personA = new Person("A");
        Person personB = new Person("B");
        personA.getPeopleILike().add(personB);
        personB.getPeopleILike().add(personA);
        session.save(personA);

        session.clear();
        assertThat(session.query(
            "MATCH (a:Person {name:'A'}) -[:LIKES]-> (b:Person {name:'B'}) " +
            "WHERE exists((b)-[:LIKES]->(a)) RETURN a, b", emptyMap()).queryResults()).hasSize(1);
    }

    @Test // DATAGRAPH-594
    public void saveOutgoingToExistingNodesSavesOutgoingRelationshipInBothDirections() {
        Person personA = new Person("A");
        Person personB = new Person("B");
        session.save(personA);
        session.save(personB);
        personA.getPeopleILike().add(personB);
        personB.getPeopleILike().add(personA);
        session.save(personA);

        session.clear();
        assertThat(session.query("MATCH (a:Person {name:'A'}) -[:LIKES]-> (b:Person {name:'B'}) " +
            "WHERE exists((b)-[:LIKES]->(a)) RETURN a, b", emptyMap()).queryResults()).hasSize(1);
    }

    @Test // DATAGRAPH-594
    public void updateOutgoingRelSavesOutgoingRelationshipInBothDirections() {
        Person personA = new Person("A");
        Person personB = new Person("B");
        Person personC = new Person("C");
        personA.getPeopleILike().add(personB);
        personB.getPeopleILike().add(personA);
        session.save(personA);

        session.clear();
        assertThat(session.query("MATCH (a:Person {name:'A'}) -[:LIKES]-> (b:Person {name:'B'}) " +
            "WHERE exists((b)-[:LIKES]->(a)) RETURN a,b", emptyMap()).queryResults()).hasSize(1);

        personA.getPeopleILike().clear();
        personA.getPeopleILike().add(personC);
        personC.getPeopleILike().add(personA);
        session.save(personA);

        session.clear();
        assertThat(session.query(""
                + "MATCH (a:Person {name:'A'})-[:LIKES]->(c:Person {name:'C'}) "
                + "MATCH (b:Person {name:'B'})-[:LIKES]->(a) "
                + " WHERE exists((c)-[:LIKES]->(a)) RETURN a, b, c", emptyMap()).queryResults()).hasSize(1);
    }

    @Test // DATAGRAPH-594
    public void updateOutgoingRelInListSavesOutgoingRelationshipInBothDirections() {
        Person personA = new Person("A");
        Person personB = new Person("B");
        Person personC = new Person("C");
        Person personD = new Person("D");
        personA.getPeopleILike().add(personB);
        personA.getPeopleILike().add(personC);
        personB.getPeopleILike().add(personA);
        personD.getPeopleILike().add(personA);

        session.save(personA);
        session.save(personB);
        session.save(personC);
        session.save(personD);

        session.clear();
        assertThat(session.query("" +
            "MATCH (a:Person {name:'A'}) -[:LIKES]-> (b:Person {name:'B'}) " +
            "MATCH (a) -[:LIKES]-> (c:Person {name:'C'}) " +
            "MATCH (d:Person {name:'D'}) -[:LIKES] ->(a) " +
            "WHERE exists((b)-[:LIKES]->(a)) " +
            "RETURN a, b, c, d", emptyMap()).queryResults()).hasSize(1);
    }

    @Test // DATAGRAPH-636, DATAGRAPH-665 (equals() on SocialUser includes every field)
    public void shouldManageRelationshipsToTheSameNodeType() {
        SocialUser userA = new SocialUser("A");
        SocialUser userB = new SocialUser("B");
        SocialUser userE = new SocialUser("E");

        Set<SocialUser> friends = new HashSet<>();
        friends.add(userB);
        friends.add(userE);

        Set<SocialUser> following = new HashSet<>();
        following.add(userB);
        following.add(userE);

        Set<SocialUser> followers = new HashSet<>();
        followers.add(userB);
        followers.add(userE);

        userA.setFollowers(followers);
        userA.setFriends(friends);
        userA.setFollowing(following);

        session.save(userA);

        session.clear();

        userA = session.load(SocialUser.class, userA.getId());
        assertThat(userA.getFriends()).hasSize(2);
        assertThat(userA.getFollowers()).hasSize(2);
        assertThat(userA.getFollowing()).hasSize(2);
    }

    @Test // GH-61
    public void shouldUseOptimizedQueryToSaveExistingRelations() {
        SocialUser userA = new SocialUser("A");
        SocialUser userB = new SocialUser("B");
        SocialUser userE = new SocialUser("E");
        session.save(userA);
        session.save(userB);
        session.save(userE);

        Set<SocialUser> friends = new HashSet<>();
        friends.add(userB);
        friends.add(userE);

        Set<SocialUser> following = new HashSet<>();
        following.add(userB);
        following.add(userE);

        Set<SocialUser> followers = new HashSet<>();
        followers.add(userB);
        followers.add(userE);

        userA.setFollowers(followers);
        userA.setFriends(friends);
        userA.setFollowing(following);

        session.save(userA);

        session.clear();

        userA = session.load(SocialUser.class, userA.getId());
        assertThat(userA.getFriends()).hasSize(2);
        assertThat(userA.getFollowers()).hasSize(2);
        assertThat(userA.getFollowing()).hasSize(2);
    }

    @Test // GH-112
    public void removeUndirectedRelationship() {
        User userA = new User("A");
        User userB = new User("B");
        userA.getFriends().add(userB);
        session.save(userA);

        String query = "MATCH (a:User {name:'A'}) -[:FRIEND]-> (b:User {name:'B'}) return a, b";
        assertThat(session.query(query,
            emptyMap(), true).queryResults()).hasSize(1);

        userA.unfriend(userB);
        session.save(userA);
        assertThat(session.query(query,
            emptyMap(), true).queryResults()).hasSize(0);
    }

    @Test // GH-305
    public void shouldBePossibleToDeleteRelationshipToPurgedNodeWithEventListener() throws Exception {
        session.register(new EventListenerAdapter());

        Person a1 = new Person("a1");
        Person a2 = new Person("a2");
        Person b = new Person("b");

        a1.setPeopleILike(Arrays.asList(b));
        a2.setPeopleILike(Arrays.asList(b));

        session.save(a1);
        session.save(a2);

        a1.setPeopleILike(Collections.emptyList());
        session.save(a1);

        a2.setPeopleILike(Collections.emptyList());
        session.save(a2);
    }
}
