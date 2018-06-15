/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.exception.core.MissingOperatorException;

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
     * @param nodeLabel  the label of the node to match
     * @param filterList a list of {@link Filter} objects defining the property filter expressions
     * @return a {@link FilteredQuery} whose statement() method contains the appropriate Cypher
     */
    public static FilteredQuery buildNodeQuery(String nodeLabel, Iterable<Filter> filterList) {
        return new NodeQueryBuilder(nodeLabel, filterList).build();
    }

    /**
     * Create a {@link FilteredQuery} which matches edges filtered by one or more property expressions
     *
     * @param relationshipType the type of the edge to match
     * @param filterList       a list of {@link Filter} objects defining the property filter expressions
     * @return a {@link FilteredQuery} whose statement() method contains the appropriate Cypher
     */
    public static FilteredQuery buildRelationshipQuery(String relationshipType, Iterable<Filter> filterList) {
        Map<String, Object> properties = new HashMap<>();
        StringBuilder sb = constructRelationshipQuery(relationshipType, filterList, properties);
        return new FilteredQuery(sb, properties);
    }

    private static StringBuilder constructRelationshipQuery(String type, Iterable<Filter> filters,
        Map<String, Object> properties) {
        List<Filter> startNodeFilters = new ArrayList<>(); //All filters that apply to the start node
        List<Filter> endNodeFilters = new ArrayList<>(); //All filters that apply to the end node
        List<Filter> relationshipFilters = new ArrayList<>(); //All filters that apply to the relationship
        String startNodeLabel = null;
        String endNodeLabel = null;
        boolean noneOperatorEncounteredInStartFilters = false;
        boolean noneOperatorEncounteredInEndFilters = false;

        for (Filter filter : filters) {
            if (filter.isNested() || filter.isDeepNested()) {
                if (filter.getBooleanOperator().equals(BooleanOperator.OR)) {
                    throw new UnsupportedOperationException(
                        "OR is not supported for nested properties on a relationship entity");
                }
                String startNestedEntityTypeLabel = filter.getNestedEntityTypeLabel();
                String endNestedEntityTypeLabel = filter.getNestedEntityTypeLabel();
                String relationshipDirection = filter.getRelationshipDirection();

                if (filter.isDeepNested()) {
                    List<Filter.NestedPathSegment> nestedPath = filter.getNestedPath();
                    Filter.NestedPathSegment firstNestedPathSegment = nestedPath.get(0);
                    Filter.NestedPathSegment lastNestedPathSegment = nestedPath.get(nestedPath.size() - 1);

                    startNestedEntityTypeLabel = firstNestedPathSegment.getNestedEntityTypeLabel();
                    endNestedEntityTypeLabel = lastNestedPathSegment.getNestedEntityTypeLabel();
                    relationshipDirection = firstNestedPathSegment.getRelationshipDirection();
                }

                if (relationshipDirection.equals(Relationship.OUTGOING)) {
                    if (filter.getBooleanOperator().equals(BooleanOperator.NONE)) {
                        if (noneOperatorEncounteredInStartFilters) {
                            throw new MissingOperatorException(
                                "BooleanOperator missing for filter with property name " + filter.getPropertyName());
                        }
                        noneOperatorEncounteredInStartFilters = true;
                    }
                    if (startNodeLabel == null) {
                        startNodeLabel = startNestedEntityTypeLabel;
                        filter.setBooleanOperator(BooleanOperator.NONE); //the first filter for the start node
                    }
                    startNodeFilters.add(filter);
                } else {
                    if (filter.getBooleanOperator().equals(BooleanOperator.NONE)) {
                        if (noneOperatorEncounteredInEndFilters) {
                            throw new MissingOperatorException(
                                "BooleanOperator missing for filter with property name " + filter.getPropertyName());
                        }
                        noneOperatorEncounteredInEndFilters = true;
                    }
                    if (endNodeLabel == null) {
                        endNodeLabel = endNestedEntityTypeLabel;
                        filter.setBooleanOperator(BooleanOperator.NONE); //the first filter for the end node
                    }
                    endNodeFilters.add(filter);
                }
            } else {
                if (relationshipFilters.size() == 0) {
                    filter.setBooleanOperator(
                        BooleanOperator.NONE); //TODO think about the importance of the first filter and stop using this as a condition to test against
                } else {
                    if (filter.getBooleanOperator().equals(BooleanOperator.NONE)) {
                        throw new MissingOperatorException(
                            "BooleanOperator missing for filter with property name " + filter.getPropertyName());
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

    private static void createRelationSubquery(String type, Map<String, Object> properties,
        List<Filter> relationshipFilters, StringBuilder query) {
        query.append(String.format("MATCH (n)-[r0:`%s`]->(m) ", type));
        if (relationshipFilters.size() > 0) {
            query.append("WHERE ");
            appendFilters(relationshipFilters, "r0", query, properties);
        }
    }

    private static void createNodeMatchSubquery(Map<String, Object> properties, List<Filter> nodeFilters,
        String nodeLabel, StringBuilder query, String nodeIdentifier) {
        if (nodeLabel != null) {
            query.append(String.format("MATCH (%s:`%s`) WHERE ", nodeIdentifier, nodeLabel));
            appendFilters(nodeFilters, nodeIdentifier, query, properties);
        }
    }

    private static void appendFilters(List<Filter> filters, String nodeIdentifier, StringBuilder query,
        Map<String, Object> properties) {
        for (Filter filter : filters) {
            query.append(filter.toCypher(nodeIdentifier, false));
            properties.putAll(filter.getFunction().parameters());
        }
    }
}
