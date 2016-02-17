/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.integration.social;

import static org.junit.Assert.assertEquals;
import static org.neo4j.ogm.testutil.GraphTestUtils.assertSameGraph;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.domain.social.Individual;
import org.neo4j.ogm.domain.social.Mortal;
import org.neo4j.ogm.domain.social.Person;
import org.neo4j.ogm.domain.social.SocialUser;
import org.neo4j.ogm.domain.social.User;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

/**
 * @author Luanne Misquitta
 */
public class SocialRelationshipsIntegrationTest {

    @ClassRule
    public static Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

	private Session session;

	@Before
	public void init() throws IOException {
		SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.social");
		session = sessionFactory.openSession(neo4jRule.url());
	}

    @After
    public void clearDatabase() {
        neo4jRule.clearDatabase();
    }

	private static GraphDatabaseService getDatabase() {
	    return neo4jRule.getGraphDatabaseService();
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

		assertSameGraph(getDatabase(), "CREATE (a:User {name:'A'}) CREATE (b:User {name:'B'}) CREATE (a)-[:FRIEND]->(b)");
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

		assertSameGraph(getDatabase(), "CREATE (a:Individual {name:'A', age: 0, code:0, bankBalance:0.0}) CREATE (b:Individual {name:'B', age:0, code:0, bankBalance:0.0}) CREATE (a)-[:FRIENDS]->(b)");
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
		assertSameGraph(getDatabase(), "CREATE (a:Person {name:'A'}) CREATE (b:Person {name:'B'}) CREATE (a)-[:LIKES]->(b)");

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
		assertSameGraph(getDatabase(), "CREATE (a:Mortal {name:'A'}) CREATE (b:Mortal {name:'B'}) CREATE (a)<-[:KNOWN_BY]-(b)");

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

		assertSameGraph(getDatabase(), "CREATE (a:Person {name:'A'}) CREATE (b:Person {name:'B'}) " +
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
		assertSameGraph(getDatabase(), "CREATE (a:Person {name:'A'}) CREATE (b:Person {name:'B'}) " +
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
		assertSameGraph(getDatabase(), "CREATE (a:Person {name:'A'}) CREATE (b:Person {name:'B'}) " +
				"CREATE (a)-[:LIKES]->(b) CREATE (b)-[:LIKES]->(a)");

		personA.getPeopleILike().clear();
		personA.getPeopleILike().add(personC);
		personC.getPeopleILike().add(personA);
		session.save(personA);
		assertSameGraph(getDatabase(), "CREATE (a:Person {name:'A'}) CREATE (b:Person {name:'B'}) CREATE (c:Person {name:'C'}) " +
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
		assertSameGraph(getDatabase(), "CREATE (a:Person {name:'A'}) CREATE (b:Person {name:'B'}) CREATE (c:Person {name:'C'}) CREATE (d:Person {name:'D'})" +
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
		assertEquals(2, userA.getFriends().size());
		assertEquals(2, userA.getFollowers().size());
		assertEquals(2, userA.getFollowing().size());
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
		assertEquals(2, userA.getFriends().size());
		assertEquals(2, userA.getFollowers().size());
		assertEquals(2, userA.getFollowing().size());
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

		assertSameGraph(getDatabase(), "CREATE (a:User {name:'A'}) CREATE (b:User {name:'B'}) CREATE (a)-[:FRIEND]->(b)");

		userA.unfriend(userB);
		session.save(userA);
		assertSameGraph(getDatabase(), "CREATE (a:User {name:'A'}) CREATE (b:User {name:'B'})");

	}

}
