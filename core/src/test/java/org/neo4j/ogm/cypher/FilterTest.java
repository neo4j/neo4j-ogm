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

package org.neo4j.ogm.cypher;

import static org.junit.Assert.*;

import org.junit.Test;
import org.neo4j.ogm.cypher.function.DistanceComparison;
import org.neo4j.ogm.cypher.function.DistanceFromPoint;
import org.neo4j.ogm.cypher.function.FilterFunction;


public class FilterTest {

	@Test
	public void toCypher() {
		Filter filter = new Filter("moons", 23);
		filter.setBooleanOperator(BooleanOperator.AND);
		filter.setComparisonOperator(ComparisonOperator.LESS_THAN);
		assertEquals("WHERE n.`moons` < { `moons_0` } ", filter.toCypher("n", true));
	}

	@Test
	public void toCypher_function() {
		FilterFunction function = new DistanceComparison(new DistanceFromPoint(37.4, 112.1, 1000.0));
		Filter filter = new Filter(function);
		filter.setBooleanOperator(BooleanOperator.AND);
		filter.setComparisonOperator(ComparisonOperator.LESS_THAN);
		filter.setNegated(true);
		assertEquals("WHERE NOT(distance(point(n),point({latitude:{lat}, longitude:{lon}})) < {distance} ) ", filter.toCypher("n", true));
	}


}