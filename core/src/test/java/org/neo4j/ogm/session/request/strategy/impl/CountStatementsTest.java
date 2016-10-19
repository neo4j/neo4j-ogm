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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.session.request.strategy.AggregateStatements;

/**
 * @author vince
 */
public class CountStatementsTest {

	private AggregateStatements statements = new CountStatements();

	@Test
	public void testCountAll() throws Exception {
		expect("", statements.countAll().getStatement());
	}

	@Test
	public void testCountNodes() throws Exception {
		expect("", statements.countNodes().getStatement());
	}

	@Test
	public void testCountNodesWithLabel() throws Exception {
		expect("", statements.countNodes("Person").getStatement());
	}

	@Test
	public void testCountNodesWithMultipleLabels() throws Exception {
		expect("MATCH (n:`Person`:`Candidate`) RETURN COUNT(n)", statements.countNodes(Arrays.asList(new String[]{"Person", "Candidate"})).getStatement());
	}

	@Test
	public void testCountNodesWithLabelAndFilters() throws Exception {
		Filters filters = new Filters();
		expect("", statements.countNodes("Person", filters).getStatement());

	}

	@Test
	public void testCountEdges() throws Exception {
		expect("", statements.countEdges().getStatement());
	}

	@Test
	public void testCountEdgesWithType() throws Exception {
		expect("", statements.countEdges("IN_CONSTITUENCY").getStatement());
	}

	@Test
	public void testCountEdgesWithTypeAndFilters() throws Exception {
		Filters filters = new Filters();
		expect("", statements.countEdges("IN_CONSTITUENCY", filters).getStatement());
	}

	@Test
	public void testCountEdgesWithSpecificPath() throws Exception {
		expect("MATCH (:`StartNode`)-[r:`TYPE`]->(:`EndNode`) RETURN count(r)", statements.countEdges("StartNode", "TYPE", "EndNode").getStatement());

	}

	private static void expect(String expected, String actual) {
		Assert.assertEquals(expected, actual);
	}
}