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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.domain.cineasts.partial.Actor;
import org.neo4j.ogm.domain.cineasts.partial.Movie;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.GraphTestUtils;
import org.neo4j.ogm.testutil.IntegrationTestRule;

/**
 * The purpose of these tests is to describe the behaviour of the
 * mapper when a RelationshipEntity object is not referenced by
 * both of its Related entities.
 *
 * @author Vince Bickers
 */
public class RelationshipEntityPartialMappingTest {

	@Rule
	public IntegrationTestRule testServer = new IntegrationTestRule(Components.driver());

	private static final SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.partial");

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
	public void testCreateActorRoleAndMovie() {

		Actor keanu = new Actor("Keanu Reeves");
		Movie matrix = new Movie("The Matrix");

		// note: this does not establish a role relationsip on the matrix
		keanu.addRole("Neo", matrix);

		session.save(keanu);
		GraphTestUtils.assertSameGraph(getDatabase(),
				"create (a:Actor {name:'Keanu Reeves'}) " +
						"create (m:Movie {name:'The Matrix'}) " +
						"create (a)-[:ACTS_IN {played:'Neo'}]->(m)");
	}
}
