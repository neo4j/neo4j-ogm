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


public class FilterTest {

	@Test
	public void toCypher() {
		Filter filter = new Filter("moons", 23);
		filter.setBooleanOperator(BooleanOperator.AND);
		filter.setComparisonOperator(ComparisonOperator.LESS_THAN);
		assertEquals("WHERE n.`moons` < { `moons` } ", filter.toCypher("n", true));
	}

	@Test
	public void toCypher_function() {
		Filter filter = new Filter(FilterFunction.DISTANCE, new DistanceComparison(37.4, 112.1, 1000.0));
		filter.setBooleanOperator(BooleanOperator.AND);
		filter.setComparisonOperator(ComparisonOperator.LESS_THAN);
		filter.setNegated(true);
		assertEquals("WHERE NOT(distance(point(n),point({latitude:{lat}, longitude:{lon}})) < {distance} ) ", filter.toCypher("n", true));
	}

	@Test
	public void setValue_shouldThrowIllegalArgumentExceptionForInvalidDistanceType() {
		try {
			new Filter(FilterFunction.DISTANCE, 23);
			fail("Should have thrown exception");
		} catch (IllegalArgumentException e) {
			assertEquals("Filter function is DISTANCE therefore value must be a type of DistanceComparison", e.getMessage());
		}
	}
}