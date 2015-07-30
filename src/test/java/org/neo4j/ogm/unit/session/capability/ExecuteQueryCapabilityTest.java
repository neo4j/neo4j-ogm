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

package org.neo4j.ogm.unit.session.capability;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.result.Result;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

/**
 * @author Luanne Misquitta
 */
public class ExecuteQueryCapabilityTest {

	@ClassRule
	public static Neo4jIntegrationTestRule databaseServerRule = new Neo4jIntegrationTestRule();

	private Session session;

	@Before
	public void init() throws IOException {
		session = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated").openSession(databaseServerRule.url());
		importCineasts();
	}

	private static void importCineasts() {
		databaseServerRule.loadClasspathCypherScriptFile("org/neo4j/ogm/cql/cineasts.cql");
	}

	@After
	public void clearDatabase() {
		session.purgeDatabase();
	}

	/**
	 * @see DATAGRAPH-697
	 */
	@Test
	public void shouldQueryForArbitraryDataUsingBespokeParameterisedCypherQuery() {
		session.save(new Actor("Helen Mirren"));
		Actor alec = new Actor("Alec Baldwin");
		session.save(alec);
		session.save(new Actor("Matt Damon"));

		Iterable<Map<String,Object>> resultsIterable = session.query("MATCH (a:Actor) WHERE ID(a)={param} RETURN a.name as name",
				Collections.<String, Object>singletonMap("param", alec.getId())); //make sure the change is backward compatible
		assertNotNull("Results are empty", resultsIterable);
		Map<String,Object> row =  resultsIterable.iterator().next();
		assertEquals("Alec Baldwin", row.get("name"));

		Result results = session.query("MATCH (a:Actor) WHERE ID(a)={param} RETURN a.name as name",
				Collections.<String, Object>singletonMap("param", alec.getId()));
		assertNotNull("Results are empty", results);
		assertEquals("Alec Baldwin", results.iterator().next().get("name"));
	}



	/**
	 * @see DATAGRAPH-697
	 */
	@Test(expected = RuntimeException.class)
	public void readOnlyQueryMustBeReadOnly() {
		session.save(new Actor("Jeff"));
		session.query("MATCH (a:Actor) SET a.age={age}", MapUtil.map("age", 5), true);
	}


	/**
	 * @see DATAGRAPH-697
	 */
	@Test
	public void modifyingQueryShouldReturnStatistics() {
		session.save(new Actor("Jeff"));
		session.save(new Actor("John"));
		session.save(new Actor("Colin"));
		Result result = session.query("MATCH (a:Actor) SET a.age={age}", MapUtil.map("age", 5), false);
		assertNotNull(result);
		assertNotNull(result.queryStatistics());
		assertEquals(3, result.queryStatistics().getPropertiesSet());


		result = session.query("MATCH (a:Actor) SET a.age={age}", MapUtil.map("age", 5));
		assertNotNull(result);
		assertNotNull(result.queryStatistics());
		assertEquals(3, result.queryStatistics().getPropertiesSet());

	}

	/**
	 * @see DATAGRAPH-697
	 */
	@Test
	public void modifyingQueryShouldReturnResultsWithStatistics() {
		session.save(new Actor("Jeff"));
		session.save(new Actor("John"));
		session.save(new Actor("Colin"));
		Result result = session.query("MATCH (a:Actor) SET a.age={age} RETURN a.name", MapUtil.map("age", 5), false);
		assertNotNull(result);
		assertNotNull(result.queryStatistics());
		assertEquals(3, result.queryStatistics().getPropertiesSet());
		List<String> names = new ArrayList<>();

		Iterator<Map<String,Object>> namesIterator = result.queryResults().iterator();
		while(namesIterator.hasNext()) {
			names.add((String)namesIterator.next().get("a.name"));
		}

		assertEquals(3, names.size());
		assertTrue(names.contains("Jeff"));
		assertTrue(names.contains("John"));
		assertTrue(names.contains("Colin"));

		result = session.query("MATCH (a:Actor) SET a.age={age} RETURN a.name, a.age", MapUtil.map("age", 5));
		assertNotNull(result);
		assertNotNull(result.queryStatistics());
		assertEquals(3, result.queryStatistics().getPropertiesSet());
		names = new ArrayList<>();

		namesIterator = result.queryResults().iterator();
		while(namesIterator.hasNext()) {
			Map<String,Object> row = namesIterator.next();
			names.add((String)row.get("a.name"));
			assertEquals(5, row.get("a.age"));
		}

		assertEquals(3, names.size());
		assertTrue(names.contains("Jeff"));
		assertTrue(names.contains("John"));
		assertTrue(names.contains("Colin"));
	}

	/**
	 * @see DATAGRAPH-697
	 */
	@Test
	public void readOnlyQueryShouldNotReturnStatistics() {
		session.save(new Actor("Jeff"));
		session.save(new Actor("John"));
		session.save(new Actor("Colin"));
		Result result = session.query("MATCH (a:Actor) RETURN a.name", Collections.EMPTY_MAP, true);
		assertNotNull(result);
		assertNull(result.queryStatistics());

		List<String> names = new ArrayList<>();

		Iterator<Map<String,Object>> namesIterator = result.queryResults().iterator();
		while(namesIterator.hasNext()) {
			names.add((String)namesIterator.next().get("a.name"));
		}

		assertEquals(3, names.size());
		assertTrue(names.contains("Jeff"));
		assertTrue(names.contains("John"));
		assertTrue(names.contains("Colin"));

	}

	/**
	 * @see DATAGRAPH-697
	 */
	@Test
	public void modifyingQueryShouldBePermittedWhenQueryingForObject() {
		session.save(new Actor("Jeff"));
		session.save(new Actor("John"));
		session.save(new Actor("Colin"));
		Actor jeff = session.queryForObject(Actor.class,"MATCH (a:Actor {name:{name}}) set a.age={age} return a", MapUtil.map("name","Jeff","age",40));
		assertNotNull(jeff);
		assertEquals("Jeff", jeff.getName());
	}

	/**
	 * @see DATAGRAPH-697
	 */
	@Test
	public void modifyingQueryShouldBePermittedWhenQueryingForObjects() {
		session.save(new Actor("Jeff"));
		session.save(new Actor("John"));
		session.save(new Actor("Colin"));
		Iterable<Actor> actors = session.query(Actor.class,"MATCH (a:Actor) set a.age={age} return a", MapUtil.map("age",40));
		assertNotNull(actors);

		List<String> names = new ArrayList<>();

		Iterator<Actor> actorIterator = actors.iterator();
		while(actorIterator.hasNext()) {
			names.add(actorIterator.next().getName());
		}

		assertEquals(3, names.size());
		assertTrue(names.contains("Jeff"));
		assertTrue(names.contains("John"));
		assertTrue(names.contains("Colin"));
	}

}
