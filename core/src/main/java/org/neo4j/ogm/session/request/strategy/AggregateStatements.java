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

package org.neo4j.ogm.session.request.strategy;

import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.CypherQuery;

/**
 * @author vince
 */
public interface AggregateStatements {

	/**
	 * construct a query to count all nodes
	 * @return a {@link CypherQuery}
	 */
	CypherQuery countNodes();

	/**
	 * construct queries to count all nodes with the specified label
	 * @param label the label attached to the object
	 * @return a {@link CypherQuery}
	 */
	CypherQuery countNodes(String label);

	/**
	 * construct queries to count all nodes with the specified label
	 * @param labels the labels attached to the object
	 * @return a {@link CypherQuery}
	 */
	CypherQuery countNodes(Iterable<String> labels);

	/**
	 * construct queries to count all nodes with the specified label that match the specified filters
	 * @param label the label value to filter on
	 * @param filters additional parameters to filter on
	 * @return a {@link CypherQuery}
	 */

	CypherQuery countNodes(String label, Filters filters);

	/**
	 * construct a query to count all relationships
	 * @return a {@link CypherQuery}
	 */
	CypherQuery countEdges();

	/**
	 * construct queries to count all nodes with the specified label
	 * @param type the relationship type
	 * @return a {@link CypherQuery}
	 */
	CypherQuery countEdges(String type);

	/**
	 * construct queries to count all relationships with the specified type that match the specified filters
	 * @param type the relationship type to filter on
	 * @param filters additional parameters to filter on
	 * @return a {@link CypherQuery}
	 */
	CypherQuery countEdges(String type, Filters filters);

	/**
	 * construct queries to count all single-length paths with the specified start label, relationship type and end label that match the specified filters
	 * @param startLabel the start node label to filter on
	 * @param relationshipType the type of relationship to filter on
	 * @param endLabel the end node label to filter on
	 * @return a {@link CypherQuery}
	 */
	CypherQuery countEdges(String startLabel, String relationshipType, String endLabel);

}
