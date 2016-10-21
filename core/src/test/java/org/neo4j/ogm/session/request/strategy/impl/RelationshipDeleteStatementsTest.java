/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.session.request.strategy.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;

/**
 * @author vince
 */
public class RelationshipDeleteStatementsTest {

	private final DeleteStatements statements = new RelationshipDeleteStatements();

	@Test
	public void testDeleteOne() throws Exception {
		assertEquals("MATCH (n)-[r]->() WHERE ID(r) = { id } DELETE r", statements.delete(0L).getStatement());
	}

	@Test
	public void testDeleteMany() throws Exception {
		assertEquals("MATCH (n)-[r]->() WHERE ID(r) IN { ids } DELETE r", statements.delete(Arrays.asList(1L, 2L)).getStatement());
	}

	@Test
	public void testDeleteAll() throws Exception {
		assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r", statements.deleteAll().getStatement());
	}

	@Test
	public void testDeleteAllAndCount() throws Exception {
		assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r RETURN COUNT(r)", statements.deleteAllAndCount().getStatement());
	}

	@Test
	public void testDeleteAllAndList() throws Exception {
		assertEquals("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r RETURN ID(r)", statements.deleteAllAndList().getStatement());
	}

	@Test
	public void testDeleteWithType() throws Exception {
		assertEquals("MATCH (n)-[r:`TRAFFIC_WARDEN`]-() DELETE r", statements.delete("TRAFFIC_WARDEN").getStatement());
	}

	@Test
	public void testDeleteWithTypeAndCount() throws Exception {
		assertEquals("MATCH (n)-[r:`TRAFFIC_WARDEN`]-() DELETE r RETURN COUNT(r)", statements.deleteAndCount("TRAFFIC_WARDEN").getStatement());
	}

	@Test
	public void testDeleteWithTypeAndList() throws Exception {
		assertEquals("MATCH (n)-[r:`TRAFFIC_WARDEN`]-() DELETE r RETURN ID(r)", statements.deleteAndList("TRAFFIC_WARDEN").getStatement());
	}

	@Test
	public void testDeleteWithTypeAndFilters() throws Exception {
		CypherQuery query = statements.delete("INFLUENCE", new Filters().add("score", -12.2));
		assertEquals("MATCH (n)-[r:`INFLUENCE`]->(m) WHERE r.`score` = { `score` }  DELETE r", query.getStatement());
	}

	@Test
	public void testDeleteWithTypeAndFiltersAndCount() throws Exception {
		CypherQuery query = statements.deleteAndCount("INFLUENCE", new Filters().add("score", -12.2));
		assertEquals("MATCH (n)-[r:`INFLUENCE`]->(m) WHERE r.`score` = { `score` }  DELETE r RETURN COUNT(r)", query.getStatement());
	}

	@Test
	public void testDeleteWithTypeAndFiltersAndList() throws Exception {
		CypherQuery query = statements.deleteAndList("INFLUENCE", new Filters().add("score", -12.2));
		assertEquals("MATCH (n)-[r:`INFLUENCE`]->(m) WHERE r.`score` = { `score` }  DELETE r RETURN ID(r)", query.getStatement());
	}
}