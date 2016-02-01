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

package org.neo4j.ogm.integration.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.tree.Entity;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

/**
 * @author Luanne Misquitta
 */
public class TreeIntegrationTest {

	@ClassRule
	public static Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

	private Session session;


	@Before
	public void init() throws IOException {
		session = new SessionFactory("org.neo4j.ogm.domain.tree").openSession(neo4jRule.url());
	}

	@After
	public void teardown() {
		session.purgeDatabase();
	}

	/**
	 * @see DATAGRAPH-731
	 */
	@Test
	public void shouldCreateTreeProperly() {
		Entity parent = new Entity("parent");
		Entity child01 = new Entity("child01").setParent(parent);
		Entity child02 = new Entity("child02").setParent(parent);

		session.save(parent);

		session.clear();

		parent = session.load(Entity.class, parent.getId());
		assertNotNull(parent);
		assertEquals(2, parent.getChildren().size());
		assertNull(parent.getParent());
		List<String> childNames = new ArrayList<>();
		for(Entity child : parent.getChildren()) {
			childNames.add(child.getName());
			assertEquals(parent.getName(),child.getParent().getName());
		}
		assertTrue(childNames.contains(child01.getName()));
		assertTrue(childNames.contains(child02.getName()));
	}

	/**
	 * @see DATAGRAPH-731
	 */
	@Test
	public void shouldLoadTreeProperly() {
		String cypher = "CREATE (parent:Entity {name:'parent'}) CREATE (child1:Entity {name:'c1'}) CREATE (child2:Entity {name:'c2'}) CREATE (child1)-[:REL]->(parent) CREATE (child2)-[:REL]->(parent)";
		session.execute(cypher);
		session.clear();
		Entity parent = session.loadAll(Entity.class, new Filter("name","parent")).iterator().next();
		assertNotNull(parent);
		assertEquals(2, parent.getChildren().size());
		assertNull(parent.getParent());
		List<String> childNames = new ArrayList<>();
		for(Entity child : parent.getChildren()) {
			childNames.add(child.getName());
			assertEquals(parent.getName(),child.getParent().getName());
		}
		assertTrue(childNames.contains("c1"));
		assertTrue(childNames.contains("c2"));
	}

	/**
	 * @see Issue 88
	 */
	@Test
	public void shouldMapElementsToTreeSetProperly() {
		String cypher = "CREATE (parent:Entity {name:'parent'}) CREATE (child1:Entity {name:'c2'}) CREATE (child2:Entity {name:'c1'}) CREATE (child1)-[:REL]->(parent) CREATE (child2)-[:REL]->(parent)";
		session.query(cypher, Utils.map());
		session.clear();
		Entity parent = session.loadAll(Entity.class, new Filter("name", "parent")).iterator().next();
		assertNotNull(parent);
		assertEquals(2, parent.getChildren().size());
		assertNull(parent.getParent());
		List<String> childNames = new ArrayList<>();
		for (Entity child : parent.getChildren()) {
			childNames.add(child.getName());
			assertEquals(parent.getName(), child.getParent().getName());
		}
		assertEquals("c1", childNames.get(0));
		assertEquals("c2", childNames.get(1));
	}
}
