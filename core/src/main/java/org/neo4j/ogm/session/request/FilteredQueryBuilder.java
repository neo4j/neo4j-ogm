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

package org.neo4j.ogm.session.request;

import java.util.*;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.exception.MissingOperatorException;

/**
 * All statements that take a {@link org.neo4j.ogm.cypher.Filters} parameter delegate the generation of the appropriate
 * Cypher to this class
 * The FilteredQueryBuilder, as its name suggests, returns instances of {@link FilteredQuery}
 *
 * @author vince
 */
public class FilteredQueryBuilder {

	/**
	 * Create a {@link FilteredQuery} which matches nodes filtered by one or more property expressions
	 *
	 * @param nodeLabel the label of the node to match
	 * @param filterList a list of {@link Filter} objects defining the property filter expressions
	 * @return a {@link FilteredQuery} whose statement() method contains the appropriate Cypher
	 */
	public static FilteredQuery buildNodeQuery(String nodeLabel, Iterable<Filter> filterList) {
		Map<String, Object> properties = new HashMap<>();
		StringBuilder sb = constructNodeQuery(nodeLabel, filterList, properties);
		return new FilteredQuery(sb, properties);
	}

	/**
	 * Create a {@link FilteredQuery} which matches edges filtered by one or more property expressions
	 *
	 * @param relationshipType the type of the edge to match
	 * @param filterList a list of {@link Filter} objects defining the property filter expressions
	 * @return a {@link FilteredQuery} whose statement() method contains the appropriate Cypher
	 */
	public static FilteredQuery buildRelationshipQuery(String relationshipType, Iterable<Filter> filterList) {
		Map<String, Object> properties = new HashMap<>();
		StringBuilder sb = constructRelationshipQuery(relationshipType, filterList, properties);
		return new FilteredQuery(sb, properties);
	}

	private static StringBuilder constructNodeQuery(String label, Iterable<Filter> filters,
	                                                Map<String, Object> properties) {

		MatchClauseKey matchClauseKey = new MatchClauseKey("n", label);

		//All individual MATCH classes, grouped by node label
		Map<MatchClauseKey, StringBuilder> matchClauses = new LinkedHashMap<>();

		//All relationship clauses
		List<StringBuilder> relationshipClauses = new ArrayList<>();

		//Create a match required to support the node entity we're supposed to return
		createOrFetchNodeQueryMatchClause(matchClauseKey, matchClauses);
		// Create the required WHERE clauses

		createNodePredicateClauses(filters, properties, matchClauses, relationshipClauses, matchClauseKey);
		//Construct the final query by appending all match clauses followed by all relationship clauses
		return buildQuery(matchClauses, relationshipClauses);
	}

	private static void createNodePredicateClauses(Iterable<Filter> filters,
	                                               Map<String, Object> properties,
	                                               Map<MatchClauseKey, StringBuilder> matchClauses,
	                                               List<StringBuilder> relationshipClauses,
	                                               MatchClauseKey matchClauseKey) {

		int matchClauseId = 0;
		boolean noneOperatorEncountered = false;
		Map<String, String> matchClauseIdentifiers = new HashMap<>(); //Mapping of the node label to the identifier used in the query

		for (Filter filter : filters) {
			StringBuilder matchClause;
			if (filter.getBooleanOperator().equals(BooleanOperator.NONE)) {
				if (noneOperatorEncountered) {
					throw new MissingOperatorException("BooleanOperator missing for filter with property name " + filter.getPropertyName() + ". Only the first filter may not specify the BooleanOperator.");
				}
				noneOperatorEncountered = true;
			}
			if (filter.isNested()) {
				if (filter.getBooleanOperator().equals(BooleanOperator.OR)) {
					throw new UnsupportedOperationException("OR is not supported for nested properties on an entity");
				}
				matchClauseKey.setNodeIdentifier("m" + matchClauseId); //Each nested filter produces a unique id for each type of node label
				if (filter.isNestedRelationshipEntity()) {
					//There is no match clause for a relationship entity, instead, we append parameters to the relationship
					matchClause = constructNodeQueryRelationshipClause(filter, matchClauseKey.getNodeIdentifier());
					matchClauses.put(matchClauseKey, matchClause);
					matchClauseKey.setNodeIdentifier("r"); //TODO this implies support for querying by one relationship entity only
				} else {
					if (matchClauseIdentifiers.containsKey(filter.getNestedEntityTypeLabel())) {
						//Use the node identifier already created for nodes with this label
						matchClauseKey.setNodeIdentifier(matchClauseIdentifiers.get(filter.getNestedEntityTypeLabel()));
					} else {
						//This node identifier  has not been constructed yet, so do so and also construct its' relationship clause
						matchClauseKey.setLabel(filter.getNestedEntityTypeLabel());
						matchClauseIdentifiers.put(filter.getNestedEntityTypeLabel(), matchClauseKey.getNodeIdentifier());
						relationshipClauses.add(constructNodeQueryRelationshipClause(filter, matchClauseKey.getNodeIdentifier()));
					}
					matchClause = createOrFetchNodeQueryMatchClause(matchClauseKey, matchClauses);
				}
				matchClauseId++;
			} else {
				//If the filter is not nested, it belongs to the node we're returning
				matchClauseKey.setNodeIdentifier("n");
				matchClause = createOrFetchNodeQueryMatchClause(matchClauseKey, matchClauses);
			}
			matchClause.append(filter.toCypher(matchClauseKey.getNodeIdentifier(), matchClause.indexOf(" WHERE ") == -1));
			properties.putAll(filter.parameters());
		}
	}

	private static StringBuilder buildQuery(Map<MatchClauseKey, StringBuilder> matchClauses,
	                                        List<StringBuilder> relationshipClauses) {
		StringBuilder query = new StringBuilder();
		for (StringBuilder matchClause : matchClauses.values()) {
			query.append(matchClause);
		}
		for (StringBuilder relationshipClause : relationshipClauses) {
			query.append(relationshipClause);
		}
		return query;
	}

	/**
	 * Construct a relationship match clause for a filter
	 *
	 * @param filter the {@link Filter}
	 * @param nodeIdentifier the node identifier used for the other node of the relationship
	 * @return the relationship clause
	 */
	private static StringBuilder constructNodeQueryRelationshipClause(Filter filter, String nodeIdentifier) {
		StringBuilder relationshipMatch;
		relationshipMatch = new StringBuilder("MATCH (n)");
		if (filter.getRelationshipDirection().equals(Relationship.INCOMING)) {
			relationshipMatch.append("<");
		}
		relationshipMatch.append(String.format("-[%s:`%s`]-", filter.isNestedRelationshipEntity() ? "r" : "", filter.getRelationshipType()));
		if (filter.getRelationshipDirection().equals(Relationship.OUTGOING)) {
			relationshipMatch.append(">");
		}
		relationshipMatch.append(String.format("(%s) ", nodeIdentifier));
		return relationshipMatch;
	}

	/**
	 * Create of fetch an existing match clause for a node with a given label and node identifier
	 *
	 * @param key The MatchClauseKey (identifier and label)
	 * @param matchClauses Map of existing match clauses, with key=node label and value=match clause
	 * @return the match clause
	 */
	private static StringBuilder createOrFetchNodeQueryMatchClause(MatchClauseKey key,
	                                                               Map<MatchClauseKey, StringBuilder> matchClauses) {
		if (matchClauses.containsKey(key)) {
			return matchClauses.get(key);
		}
		StringBuilder matchClause = new StringBuilder();

		matchClause.append(String.format("MATCH (%s:`%s`) ", key.getNodeIdentifier(), key.getLabel()));
		matchClauses.put(key, matchClause);
		return matchClause;
	}

	private static StringBuilder constructRelationshipQuery(String type, Iterable<Filter> filters, Map<String, Object> properties) {
		List<Filter> startNodeFilters = new ArrayList<>(); //All filters that apply to the start node
		List<Filter> endNodeFilters = new ArrayList<>(); //All filters that apply to the end node
		List<Filter> relationshipFilters = new ArrayList<>(); //All filters that apply to the relationship
		String startNodeLabel = null;
		String endNodeLabel = null;
		boolean noneOperatorEncounteredInStartFilters = false;
		boolean noneOperatorEncounteredInEndFilters = false;

		for (Filter filter : filters) {
			if (filter.isNested()) {
				if (filter.getBooleanOperator().equals(BooleanOperator.OR)) {
					throw new UnsupportedOperationException("OR is not supported for nested properties on a relationship entity");
				}
				if (filter.getRelationshipDirection().equals(Relationship.OUTGOING)) {
					if (filter.getBooleanOperator().equals(BooleanOperator.NONE)) {
						if (noneOperatorEncounteredInStartFilters) {
							throw new MissingOperatorException("BooleanOperator missing for filter with property name " + filter.getPropertyName());
						}
						noneOperatorEncounteredInStartFilters = true;
					}
					if (startNodeLabel == null) {
						startNodeLabel = filter.getNestedEntityTypeLabel();
						filter.setBooleanOperator(BooleanOperator.NONE); //the first filter for the start node
					}
					startNodeFilters.add(filter);
				} else {
					if (filter.getBooleanOperator().equals(BooleanOperator.NONE)) {
						if (noneOperatorEncounteredInEndFilters) {
							throw new MissingOperatorException("BooleanOperator missing for filter with property name " + filter.getPropertyName());
						}
						noneOperatorEncounteredInEndFilters = true;
					}
					if (endNodeLabel == null) {
						endNodeLabel = filter.getNestedEntityTypeLabel();
						filter.setBooleanOperator(BooleanOperator.NONE); //the first filter for the end node
					}
					endNodeFilters.add(filter);
				}
			} else {
				if (relationshipFilters.size() == 0) {
					filter.setBooleanOperator(BooleanOperator.NONE); //TODO think about the importance of the first filter and stop using this as a condition to test against
				} else {
					if (filter.getBooleanOperator().equals(BooleanOperator.NONE)) {
						throw new MissingOperatorException("BooleanOperator missing for filter with property name " + filter.getPropertyName());
					}
				}
				relationshipFilters.add(filter);
			}
		}

		StringBuilder query = new StringBuilder();
		createNodeMatchSubquery(properties, startNodeFilters, startNodeLabel, query, "n");
		createNodeMatchSubquery(properties, endNodeFilters, endNodeLabel, query, "m");
		createRelationSubquery(type, properties, relationshipFilters, query);
		return query;
	}

	private static void createRelationSubquery(String type, Map<String, Object> properties, List<Filter> relationshipFilters, StringBuilder query) {
		query.append(String.format("MATCH (n)-[r:`%s`]->(m) ", type));
		if (relationshipFilters.size() > 0) {
			query.append("WHERE ");
			appendFilters(relationshipFilters, "r", query, properties);
		}
	}

	private static void createNodeMatchSubquery(Map<String, Object> properties, List<Filter> nodeFilters, String nodeLabel, StringBuilder query, String nodeIdentifier) {
		if (nodeLabel != null) {
			query.append(String.format("MATCH (%s:`%s`) WHERE ", nodeIdentifier, nodeLabel));
			appendFilters(nodeFilters, nodeIdentifier, query, properties);
		}
	}

	private static void appendFilters(List<Filter> filters, String nodeIdentifier, StringBuilder query, Map<String, Object> properties) {
		for (Filter filter : filters) {
			query.append(filter.toCypher(nodeIdentifier, false));
			properties.putAll(filter.getFunction().parameters());
		}
	}
}
