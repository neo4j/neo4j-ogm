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

package org.neo4j.ogm.mapper;


import java.io.IOException;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.domain.election.Candidate;
import org.neo4j.ogm.domain.election.Voter;
import org.neo4j.ogm.domain.policy.Person;
import org.neo4j.ogm.domain.policy.Policy;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.IntegrationTestRule;

/**
 * @author Mark Angrish
 * @author Luanne Misquitta
 */
public class RelationshipMappingTest {


	@Rule
	public IntegrationTestRule testServer = new IntegrationTestRule(Components.driver());

	private static final SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.policy", "org.neo4j.ogm.domain.election");

	private Session session;

	@Before
	public void init() throws IOException {
		session = sessionFactory.openSession(testServer.driver());
		session.purgeDatabase();
	}

	private GraphDatabaseService getDatabase() {
		return testServer.getGraphDatabaseService();
	}


	@Test
	public void testThatABiDirectionalMappingIsEstablishedWhenAMutualRelationshipWithNoAnnotationsIsSaved() {

		Person jim = new Person("Jim");
		Policy policy = new Policy("Health");

		// create a mutual relationship
		policy.getInfluencers().add(jim);
		jim.getInfluenced().add(policy);

		// expect both objects to have a relationship to each other
		session.save(policy);
		GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (n:Policy:DomainObject {name:'Health'})-[:INFLUENCERS]->(m:Person:DomainObject {name:'Jim'})-[:INFLUENCED]->(n)");
	}

	@Test
	public void testThatAnAnnotatedRelationshipOnTwoObjectsThatIsSavedFromTheOutgoingCreatesTheCorrectRelationshipInTheGraph() {

		Person jim = new Person("Jim");
		Policy policy = new Policy("Health");

		jim.getWritten().add(policy);

		session.save(jim);
		GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (n:Policy:DomainObject {name:'Health'})<-[:WRITES_POLICY]-(m:Person:DomainObject {name:'Jim'})");
	}

	@Test
	public void testThatAnAnnotatedRelationshipSavedFromTheIncomingSideCreatesTheCorrectRelationshipInTheGraph() {

		Person jim = new Person("Jim");
		Policy policy = new Policy("Health");

		policy.getWriters().add(jim);

		// we a single relationship, outgoing from person to policy to be established
		session.save(policy);
		GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (n:Policy:DomainObject {name:'Health'})<-[:WRITES_POLICY]-(m:Person:DomainObject {name:'Jim'})");
	}

	@Test
	public void testPersistAnnotatedSingleRelationshipMappingBothDomainObjectsParticipating() {

		Person jim = new Person("Jim");
		Policy policy = new Policy("Health");

		// establish the relationship in both directions in the domain model
		policy.getWriters().add(jim);
		jim.getWritten().add(policy);

		// verify we create only one directed relationship in the graph
		session.save(policy);
		GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (n:Policy:DomainObject {name:'Health'})<-[:WRITES_POLICY]-(m:Person:DomainObject {name:'Jim'})");
	}

	/**
	 * @see DATAGRAPH-674
	 */
	@Test
	public void testAnnotatedRelationshipTypeWhenMethodsAreJsonIgnored() {
		Person jim = new Person("Jim");
		Policy policy = new Policy("Health");

		policy.setAuthorized(jim);
		jim.setAuthorized(policy);

		session.save(jim);
		GraphTestUtils.assertSameGraph(getDatabase(), "CREATE (n:Policy:DomainObject {name:'Health'})<-[:AUTHORIZED_POLICY]-(m:Person:DomainObject {name:'Jim'})");
	}


	@Test
	public void shouldAllowVoterToChangeHerMind() {

		ExecutionEngine executionEngine = new ExecutionEngine(getDatabase());
		// create the graph
		ExecutionResult executionResult = executionEngine.execute(
				"CREATE " +
						"(a:Voter:Candidate {name:'A'}), " +
						"(b:Voter:Candidate {name:'B'}), " +
						"(v:Voter {name:'V'})-[:CANDIDATE_VOTED_FOR]->(b) " +
						"RETURN id(a) AS a_id, id(b) AS b_id, id(v) AS v_id");

		// build the object map
		Map<String, ?> results = executionResult.iterator().next();

		Long aid = (Long) results.get("a_id");
		Long bid = (Long) results.get("b_id");
		Long vid = (Long) results.get("v_id");

		Candidate a = session.load(Candidate.class, aid);

		Candidate b = session.load(Candidate.class, bid);

		Voter v = session.load(Voter.class, vid);

		// voter changes her mind
		v.candidateVotedFor = a;
		session.save(v);
		GraphTestUtils.assertSameGraph(getDatabase(),
				"CREATE (a:Voter:Candidate {name:'A'}) " +
						"CREATE (b:Voter:Candidate {name:'B'}) " +
						"CREATE (v:Voter {name:'V'})-[:CANDIDATE_VOTED_FOR]->(a)");
	}
}
