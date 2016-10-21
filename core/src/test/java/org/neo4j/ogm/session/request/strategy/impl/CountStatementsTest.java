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
import org.neo4j.ogm.session.request.strategy.AggregateStatements;

/**
 * @author vince
 */
public class CountStatementsTest {

	private AggregateStatements statements = new CountStatements();

	@Test
	public void testCountNodes() throws Exception {
		assertEquals("MATCH (n) RETURN COUNT(n)", statements.countNodes().getStatement());
	}

	@Test
	public void testCountNodesWithLabel() throws Exception {
		assertEquals("MATCH (n:`Person`) RETURN COUNT(n)", statements.countNodes("Person").getStatement());
	}

	@Test
	public void testCountNodesWithMultipleLabels() throws Exception {
		assertEquals("MATCH (n:`Person`:`Candidate`) RETURN COUNT(n)", statements.countNodes(Arrays.asList(new String[]{"Person", "Candidate"})).getStatement());
	}

	@Test
	public void testCountNodesWithLabelAndFilters() throws Exception {
		CypherQuery query = statements.countNodes("Person", new Filters().add("name", "Jim"));
		assertEquals("MATCH (n:`Person`) WHERE n.`name` = { `name_0` }  RETURN COUNT(n)", query.getStatement());
		assertEquals("{name_0=Jim}", query.getParameters().toString());

	}

	@Test
	public void testCountEdges() throws Exception {
		assertEquals("MATCH (n)-[r]->() RETURN COUNT(r)", statements.countEdges().getStatement());
	}

	@Test
	public void testCountEdgesWithType() throws Exception {
		assertEquals("MATCH (n)-[r:`IN_CONSTITUENCY`]->() RETURN COUNT(r)", statements.countEdges("IN_CONSTITUENCY").getStatement());
	}

	@Test
	public void testCountEdgesWithTypeAndFilters() throws Exception {
		CypherQuery query = statements.countEdges("INFLUENCE", new Filters().add("score", -12.2));
		assertEquals("MATCH (n)-[r:`INFLUENCE`]->(m) WHERE r.`score` = { `score` }  RETURN COUNT(n)", query.getStatement());
		assertEquals("{score=-12.2}", query.getParameters().toString());
	}

	@Test
	public void testCountEdgesWithSpecificPath() throws Exception {
		assertEquals("MATCH (:`StartNode`)-[r:`TYPE`]->(:`EndNode`) RETURN COUNT(r)", statements.countEdges("StartNode", "TYPE", "EndNode").getStatement());

	}


}