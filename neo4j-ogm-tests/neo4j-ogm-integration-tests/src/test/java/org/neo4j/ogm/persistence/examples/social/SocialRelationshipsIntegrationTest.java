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
package org.neo4j.ogm.persistence.examples.social;

import static org.assertj.core.api.Assertions.*;
import static org.neo4j.ogm.testutil.GraphTestUtils.*;

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
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Luanne Misquitta
 */
public class SocialRelationshipsIntegrationTest extends MultiDriverTestClass {

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.social");
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

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void saveUndirectedSavesOutgoingRelationship() {
        User userA = new User("A");
        User userB = new User("B");
        userA.getFriends().add(userB);
        session.save(userA);

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (a:User {name:'A'}) CREATE (b:User {name:'B'}) CREATE (a)-[:FRIEND]->(b)");
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void saveUnmarkedSavesOutgoingRelationship() {
        Individual individualA = new Individual();
        individualA.setName("A");
        Individual individualB = new Individual();
        individualB.setName("B");
        individualA.setFriends(Collections.singletonList(individualB));
        session.save(individualA);

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (a:Individual {name:'A', age: 0, code:0, bankBalance:0.0}) CREATE (b:Individual {name:'B', age:0, code:0, bankBalance:0.0}) CREATE (a)-[:FRIENDS]->(b)");
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void saveOutgoingSavesOutgoingRelationship() {
        Person personA = new Person("A");
        Person personB = new Person("B");
        personA.getPeopleILike().add(personB);
        session.save(personA);
        assertSameGraph(getGraphDatabaseService(),
            "CREATE (a:Person {name:'A'}) CREATE (b:Person {name:'B'}) CREATE (a)-[:LIKES]->(b)");
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void saveIncomingSavesIncomingRelationship() {
        Mortal mortalA = new Mortal("A");
        Mortal mortalB = new Mortal("B");
        mortalA.getKnownBy().add(mortalB);
        session.save(mortalA);
        assertSameGraph(getGraphDatabaseService(),
            "CREATE (a:Mortal {name:'A'}) CREATE (b:Mortal {name:'B'}) CREATE (a)<-[:KNOWN_BY]-(b)");
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void saveOutgoingSavesOutgoingRelationshipInBothDirections() {
        Person personA = new Person("A");
        Person personB = new Person("B");
        personA.getPeopleILike().add(personB);
        personB.getPeopleILike().add(personA);
        session.save(personA);

        assertSameGraph(getGraphDatabaseService(), "CREATE (a:Person {name:'A'}) CREATE (b:Person {name:'B'}) " +
            "CREATE (a)-[:LIKES]->(b) CREATE (b)-[:LIKES]->(a)");
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void saveOutgoingToExistingNodesSavesOutgoingRelationshipInBothDirections() {
        Person personA = new Person("A");
        Person personB = new Person("B");
        session.save(personA);
        session.save(personB);
        personA.getPeopleILike().add(personB);
        personB.getPeopleILike().add(personA);
        session.save(personA);
        assertSameGraph(getGraphDatabaseService(), "CREATE (a:Person {name:'A'}) CREATE (b:Person {name:'B'}) " +
            "CREATE (a)-[:LIKES]->(b) CREATE (b)-[:LIKES]->(a)");
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
    public void updateOutgoingRelSavesOutgoingRelationshipInBothDirections() {
        Person personA = new Person("A");
        Person personB = new Person("B");
        Person personC = new Person("C");
        personA.getPeopleILike().add(personB);
        personB.getPeopleILike().add(personA);
        session.save(personA);
        assertSameGraph(getGraphDatabaseService(), "CREATE (a:Person {name:'A'}) CREATE (b:Person {name:'B'}) " +
            "CREATE (a)-[:LIKES]->(b) CREATE (b)-[:LIKES]->(a)");

        personA.getPeopleILike().clear();
        personA.getPeopleILike().add(personC);
        personC.getPeopleILike().add(personA);
        session.save(personA);
        assertSameGraph(getGraphDatabaseService(),
            "CREATE (a:Person {name:'A'}) CREATE (b:Person {name:'B'}) CREATE (c:Person {name:'C'}) " +
                " CREATE (a)-[:LIKES]->(c) CREATE (c)-[:LIKES]->(a) CREATE (b)-[:LIKES]->(a)");
    }

    /**
     * @see DATAGRAPH-594
     */
    @Test
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
        assertSameGraph(getGraphDatabaseService(),
            "CREATE (a:Person {name:'A'}) CREATE (b:Person {name:'B'}) CREATE (c:Person {name:'C'}) CREATE (d:Person {name:'D'})"
                +
                "CREATE (a)-[:LIKES]->(b) CREATE (a)-[:LIKES]->(c) CREATE (b)-[:LIKES]->(a) CREATE (d)-[:LIKES]->(a)");
    }

    /**
     * @see DATAGRAPH-636, DATAGRAPH-665 (equals() on SocialUser includes every field)
     */
    @Test
    public void shouldManageRelationshipsToTheSameNodeType() {
        SocialUser userA = new SocialUser("A");
        SocialUser userB = new SocialUser("B");
        SocialUser userC = new SocialUser("C");
        SocialUser userD = new SocialUser("D");
        SocialUser userE = new SocialUser("E");
        SocialUser userF = new SocialUser("F");
        SocialUser userG = new SocialUser("G");

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

    /**
     * @see Issue #61
     */
    @Test
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

    /**
     * @see issue #112
     */
    @Test
    public void removeUndirectedRelationship() {
        User userA = new User("A");
        User userB = new User("B");
        userA.getFriends().add(userB);
        session.save(userA);

        assertSameGraph(getGraphDatabaseService(),
            "CREATE (a:User {name:'A'}) CREATE (b:User {name:'B'}) CREATE (a)-[:FRIEND]->(b)");

        userA.unfriend(userB);
        session.save(userA);
        assertSameGraph(getGraphDatabaseService(), "CREATE (a:User {name:'A'}) CREATE (b:User {name:'B'})");
    }

    /**
     * @see <a href="https://github.com/neo4j/neo4j-ogm/issues/305">issue 305</a>
     */
    @Test
    public void shouldBePossibleToDeleteRelationshipToPurgedNodeWithEventListener() throws Exception {
        session.register(new EventListenerAdapter());

        Person a1 = new Person("a1");
        Person a2 = new Person("a2");
        Person b = new Person("b");

        a1.setPeopleILike(Arrays.asList(b));
        a2.setPeopleILike(Arrays.asList(b));

        session.save(a1);
        session.save(a2);

        a1.setPeopleILike(Collections.<Person>emptyList());
        session.save(a1);

        a2.setPeopleILike(Collections.<Person>emptyList());
        session.save(a2);
    }
}
