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
 *
 */

package org.neo4j.ogm.integration.social;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.social.Individual;
import org.neo4j.ogm.domain.social.Mortal;
import org.neo4j.ogm.domain.social.Person;
import org.neo4j.ogm.domain.social.User;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.result.CypherException;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

/**
 * @author Luanne Misquitta
 */
public class SocialIntegrationTest
{
    @ClassRule
    public static Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

    private Session session;


    @Before
	public void init() throws IOException {
		session = new SessionFactory("org.neo4j.ogm.domain.social").openSession(neo4jRule.url());
	}

	@After
	public void clearDatabase() {
	    neo4jRule.clearDatabase();
	}

	/**
	 * @see DATAGRAPH-594
	 */
	@Test
	public void shouldFetchOnlyPeopleILike() {
		session.execute("create (p1:Person {name:'A'}) create (p2:Person {name:'B'}) create (p3:Person {name:'C'})" +
				" create (p4:Person {name:'D'}) create (p1)-[:LIKES]->(p2) create (p1)-[:LIKES]->(p3) create (p4)-[:LIKES]->(p1)", Collections.EMPTY_MAP);

		Person personA = session.loadAll(Person.class, new Filter("name", "A")).iterator().next();
		assertNotNull(personA);
		assertEquals(2, personA.getPeopleILike().size());

		Person personD = session.loadAll(Person.class, new Filter("name", "D")).iterator().next();
		assertNotNull(personD);
		assertEquals(1, personD.getPeopleILike().size());
		assertEquals(personA,personD.getPeopleILike().get(0));

	}

	/**
	 * @see DATAGRAPH-594
	 */
	@Test
	public void shouldFetchFriendsInBothDirections() {
		session.execute("create (p1:Individual {name:'A'}) create (p2:Individual {name:'B'}) create (p3:Individual {name:'C'})" +
				" create (p4:Individual {name:'D'}) create (p1)-[:FRIENDS]->(p2) create (p1)-[:FRIENDS]->(p3) create (p4)-[:FRIENDS]->(p1)", Collections.EMPTY_MAP);

		Individual individualA = session.loadAll(Individual.class, new Filter("name", "A")).iterator().next();
		assertNotNull(individualA);
		assertEquals(2, individualA.getFriends().size());

	}

	/**
	 * @see DATAGRAPH-594
	 */
	@Test
	public void shouldFetchFriendsForUndirectedRelationship() {
		session.execute("create (p1:User {name:'A'}) create (p2:User {name:'B'}) create (p3:User {name:'C'})" +
				" create (p4:User {name:'D'}) create (p1)-[:FRIEND]->(p2) create (p1)-[:FRIEND]->(p3) create (p4)-[:FRIEND]->(p1)", Collections.EMPTY_MAP);

		User userA = session.loadAll(User.class, new Filter("name", "A")).iterator().next();
		assertNotNull(userA);
		assertEquals(3, userA.getFriends().size());

		User userB = session.loadAll(User.class, new Filter("name", "B")).iterator().next();
		assertNotNull(userB);
		assertEquals(1, userB.getFriends().size());
		assertEquals(userA, userB.getFriends().get(0));

		User userD = session.loadAll(User.class, new Filter("name", "D")).iterator().next();
		assertNotNull(userD);
		assertEquals(1, userD.getFriends().size());
		assertEquals(userA, userD.getFriends().get(0));
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

		userA = session.loadAll(User.class, new Filter("name", "A")).iterator().next();
		assertNotNull(userA);
		assertEquals(3, userA.getFriends().size());

		userB = session.loadAll(User.class, new Filter("name", "B")).iterator().next();
		assertNotNull(userB);
		assertEquals(1, userB.getFriends().size());
		assertEquals(userA.getName(), userB.getFriends().get(0).getName());

		userD = session.loadAll(User.class, new Filter("name", "D")).iterator().next();
		assertNotNull(userD);
		assertEquals(1, userD.getFriends().size());
		assertEquals(userA.getName(), userD.getFriends().get(0).getName());
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
		userA = session.loadAll(Person.class, new Filter("name", "A")).iterator().next();
		assertNotNull(userA);
		assertEquals(1, userA.getPeopleILike().size());
		session.clear();
		userB = session.loadAll(Person.class, new Filter("name", "B")).iterator().next();
		assertNotNull(userB);
		assertEquals(1, userB.getPeopleILike().size());
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

		mortalA = session.loadAll(Mortal.class, new Filter("name", "A")).iterator().next();
		assertNotNull(mortalA);
		assertEquals(2, mortalA.getKnownBy().size());

		mortalB = session.loadAll(Mortal.class, new Filter("name", "B")).iterator().next();
		assertNotNull(mortalB);
		assertEquals(0, mortalB.getKnownBy().size());

		mortalC = session.loadAll(Mortal.class, new Filter("name", "C")).iterator().next();
		assertNotNull(mortalC);
		assertEquals(0, mortalC.getKnownBy().size());

		mortalD = session.loadAll(Mortal.class, new Filter("name", "D")).iterator().next();
		assertNotNull(mortalD);
		assertEquals(1, mortalD.getKnownBy().size());
		assertEquals("A", mortalD.getKnownBy().iterator().next().getName());
	}

	/**
	 * @see DATAGRAPH-594
	 */
	@Test
	public void shouldFetchIncomingKnownMortals() {
		session.execute("create (m1:Mortal {name:'A'}) create (m2:Mortal {name:'B'}) create (m3:Mortal {name:'C'})" +
				" create (m4:Mortal {name:'D'}) create (m1)<-[:KNOWN_BY]-(m2) create (m1)<-[:KNOWN_BY]-(m3) create (m4)<-[:KNOWN_BY]-(m1)", Collections.EMPTY_MAP);

		Mortal mortalA = session.loadAll(Mortal.class, new Filter("name", "A")).iterator().next();
		assertNotNull(mortalA);
		assertEquals(2, mortalA.getKnownBy().size());

		Mortal mortalB = session.loadAll(Mortal.class, new Filter("name", "B")).iterator().next();
		assertNotNull(mortalB);
		assertEquals(0, mortalB.getKnownBy().size());

		Mortal mortalC = session.loadAll(Mortal.class, new Filter("name", "C")).iterator().next();
		assertNotNull(mortalC);
		assertEquals(0, mortalC.getKnownBy().size());

		Mortal mortalD = session.loadAll(Mortal.class, new Filter("name", "D")).iterator().next();
		assertNotNull(mortalD);
		assertEquals(1, mortalD.getKnownBy().size());
		assertEquals("A", mortalD.getKnownBy().iterator().next().getName());
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
		assertEquals(1, adam.getFriends().size());

		daniela = session.load(User.class, daniela.getId());
		assertEquals(2, daniela.getFriends().size());
		List<String> friendNames = new ArrayList<>();
		for(User friend : daniela.getFriends()) {
			friendNames.add(friend.getName());
		}
		assertTrue(friendNames.contains("Adam"));
		assertTrue(friendNames.contains("Michal"));

		session.clear();

		michal = session.load(User.class, michal.getId());
		assertEquals(2, michal.getFriends().size());

		session.clear();
		vince = session.load(User.class, vince.getId());
		assertEquals(1, vince.getFriends().size());
	}

	/**
	 * @see issue 118
	 */
	@Test
	public void shouldWrapUnderlyingException() {
		session.save(new Person("Bilbo Baggins"));
		try {
			session.query(User.class, "MATCH(p:Person) WHERE u.name ~ '.*Baggins' RETURN u", Utils.map());
		}
		catch (CypherException ce) {
			assertTrue(ce.getCode().contains("Neo.ClientError.Statement.InvalidSyntax"));
			assertTrue(ce.getDescription().contains("Invalid input"));
			return;
		}
		fail("Expected a CypherException but got none");
	}

}
