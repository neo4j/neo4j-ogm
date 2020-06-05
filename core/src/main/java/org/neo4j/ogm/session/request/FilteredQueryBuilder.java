/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.session.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.neo4j.ogm.annotation.Relationship.Direction;
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.exception.core.MissingOperatorException;

/**
 * All statements that take a {@link org.neo4j.ogm.cypher.Filters} parameter delegate the generation of the appropriate
 * Cypher to this class
 * The FilteredQueryBuilder, as its name suggests, returns instances of {@link FilteredQuery}
 *
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class FilteredQueryBuilder {

    private static void validateNestedFilters(Iterable<Filter> filters) {

        Predicate<Filter> byIsNested = f -> (f.isNested() || f.isDeepNested());

        // Find all non nested filters for later reference
        List<Filter> other = StreamSupport
            .stream(filters::spliterator, Spliterator.ORDERED, false)
            .filter(byIsNested.negate()).collect(Collectors.toList());

        // Group the nested filter like the final query builder would do.
        // Ors in one inner nested filter are fine
        Map<String, List<Filter>> groupedFilters = StreamSupport
            .stream(filters::spliterator, Spliterator.ORDERED, false)
            .filter(byIsNested)
            .collect(Collectors.groupingBy(
                filter -> {
                    if (filter.isNested()) {
                        return filter.isNestedRelationshipEntity() ?
                            filter.getRelationshipType() :
                            filter.getNestedEntityTypeLabel();
                    } else {
                        return filter.getNestedPath().get(filter.getNestedPath().size() - 1).getNestedEntityTypeLabel();
                    }
                },
                TreeMap::new,
                Collectors.toList()
            ));

        Predicate<Filter> hasOrOperator = filter -> BooleanOperator.OR == filter.getBooleanOperator();
        boolean throwException = false;

        if (other.isEmpty()) {
            // If there are only nested filters, we can check if there's more than one and if they should be ordered together
            if (groupedFilters.size() > 1) {
                throwException = groupedFilters
                    .values().stream()
                    .anyMatch(l -> hasOrOperator.test(l.get(0)));
            }
        } else if (!groupedFilters.isEmpty()) {
            // Otherwise we have to have a look whether there's at least one nested filter that is support to be or'ed with
            // other filters.Wi
            throwException = other.stream().anyMatch(hasOrOperator)
                || groupedFilters.values().stream()
                .map(groupedFilter -> groupedFilter.get(0))
                .anyMatch(hasOrOperator);
        }

        if (throwException) {
            throw new UnsupportedOperationException(
                "Filters containing nested paths cannot be combined via the logical OR operator.");
        }
    }

    /**
     * Create a {@link FilteredQuery} which matches nodes filtered by one or more property expressions
     *
     * @param nodeLabel  the label of the node to match
     * @param filterList a list of {@link Filter} objects defining the property filter expressions
     * @return a {@link FilteredQuery} whose statement() method contains the appropriate Cypher
     */
    public static FilteredQuery buildNodeQuery(String nodeLabel, Iterable<Filter> filterList) {

        validateNestedFilters(filterList);
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

        validateNestedFilters(filterList);

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
                String startNestedEntityTypeLabel = filter.getNestedEntityTypeLabel();
                String endNestedEntityTypeLabel = filter.getNestedEntityTypeLabel();
                Direction relationshipDirection = filter.getRelationshipDirection();

                if (filter.isDeepNested()) {
                    List<Filter.NestedPathSegment> nestedPath = filter.getNestedPath();
                    Filter.NestedPathSegment firstNestedPathSegment = nestedPath.get(0);
                    Filter.NestedPathSegment lastNestedPathSegment = nestedPath.get(nestedPath.size() - 1);

                    startNestedEntityTypeLabel = firstNestedPathSegment.getNestedEntityTypeLabel();
                    endNestedEntityTypeLabel = lastNestedPathSegment.getNestedEntityTypeLabel();
                    relationshipDirection = firstNestedPathSegment.getRelationshipDirection();
                }

                if (relationshipDirection == Direction.OUTGOING) {
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
            properties.putAll(filter.parameters());
        }
    }
}
