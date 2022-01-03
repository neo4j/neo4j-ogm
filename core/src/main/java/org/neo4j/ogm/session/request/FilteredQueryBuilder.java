/*
 * Copyright (c) 2002-2022 "Neo4j,"
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

import org.neo4j.ogm.annotation.Relationship;
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

        // Filters are created in 3 steps: For deep nested filter, the improved version
        // NodeQueryBuilder is used. For the others the old approach still applies.
        FiltersAtStartNode outgoingDeepNestedFilters = new FiltersAtStartNode();
        FiltersAtStartNode incomingDeepNestedFilters = new FiltersAtStartNode();

        FiltersAtStartNode outgoingFilters = new FiltersAtStartNode();
        FiltersAtStartNode incomingFilters = new FiltersAtStartNode();

        List<Filter> relationshipFilters = new ArrayList<>();

        Direction initialDirection = null;
        for (Filter filter : filters) {
            if (filter.isNested() || filter.isDeepNested()) {
                if (filter.isDeepNested()) {
                    List<Filter.NestedPathSegment> nestedPath = filter.getNestedPath();

                    Filter.NestedPathSegment firstNestedPathSegment = nestedPath.get(0);
                    filter.setOwnerEntityType(firstNestedPathSegment.getPropertyType());

                    FiltersAtStartNode target;
                    if (Relationship.Direction.OUTGOING == firstNestedPathSegment.getRelationshipDirection()) {
                        target = outgoingDeepNestedFilters;
                    } else {
                        target = incomingDeepNestedFilters;
                    }

                    Filter.NestedPathSegment[] newPath = new Filter.NestedPathSegment[nestedPath.size() - 1];
                    if (nestedPath.size() > 1) {
                        // The first element will represent the owning entity, so we need to get rid of it.
                        nestedPath.subList(1, nestedPath.size()).toArray(newPath);
                    } else {
                        // The list of deep nested filters need an anchor only for relationships with one
                        // nested segments.
                        target.startNodeLabel = firstNestedPathSegment.getNestedEntityTypeLabel();
                    }
                    filter.setNestedPath(newPath);
                    target.content.add(filter);

                    if (initialDirection == null) {
                        initialDirection = firstNestedPathSegment.getRelationshipDirection();
                    }
                } else {
                    FiltersAtStartNode target;

                    Direction relationshipDirection = filter.getRelationshipDirection();
                    if (Relationship.OUTGOING == relationshipDirection) {
                        target = outgoingFilters;
                    } else {
                        target = incomingFilters;
                    }

                    if (initialDirection == null) {
                        initialDirection = filter.getRelationshipDirection();
                    }

                    addFilterToList(target, filter);
                }
            } else {
                if (relationshipFilters.size() == 0) {
                    filter.setBooleanOperator(BooleanOperator.NONE);
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
        boolean outgoingDeepNested = !outgoingDeepNestedFilters.content.isEmpty();
        if (outgoingDeepNested) {
            NodeQueryBuilder nqb = new NodeQueryBuilder(outgoingDeepNestedFilters.startNodeLabel,
                outgoingDeepNestedFilters.content, "n");
            FilteredQuery filteredQuery = nqb.build();
            query.append(filteredQuery.statement()).append(" ");
            properties.putAll(filteredQuery.parameters());
        }
        if (!incomingDeepNestedFilters.content.isEmpty()) {
            NodeQueryBuilder nqb = new NodeQueryBuilder(incomingDeepNestedFilters.startNodeLabel,
                incomingDeepNestedFilters.content, outgoingDeepNested ? "m" : "n");
            FilteredQuery filteredQuery = nqb.build();
            query.append(filteredQuery.statement()).append(" ");
            if (outgoingDeepNested) {
                query.append(", n ");
            }
            properties.putAll(filteredQuery.parameters());
        }
        createNodeMatchSubquery(properties, outgoingFilters, query, "n");
        createNodeMatchSubquery(properties, incomingFilters, query, "m");
        createRelationSubquery(type, properties, relationshipFilters, query, initialDirection);
        return query;
    }

    static class FiltersAtStartNode {

        String startNodeLabel;

        List<Filter> content = new ArrayList<>();
    }

    /**
     * Adds a filter to list, checking if all filters but the first have an operator.
     *
     * @param target The target filters
     * @param filter The filter to add
     */
    private static void addFilterToList(FiltersAtStartNode target, Filter filter) {
        if (filter.getBooleanOperator().equals(BooleanOperator.NONE) && !target.content.isEmpty()) {
            throw new MissingOperatorException(
                "BooleanOperator missing for filter with property name " + filter.getPropertyName());
        }
        target.content.add(filter);
        if (target.startNodeLabel == null) {
            target.startNodeLabel = filter.getNestedEntityTypeLabel();
        }
    }

    private static void createRelationSubquery(String type, Map<String, Object> properties,
        List<Filter> relationshipFilters, StringBuilder query, Direction initialDirection) {

        if (initialDirection == null || initialDirection == Relationship.OUTGOING) {
            query.append(String.format("MATCH (n)-[r0:`%s`]->(m) ", type));
        } else {
            query.append(String.format("MATCH (n)<-[r0:`%s`]-(m) ", type));
        }
        if (relationshipFilters.size() > 0) {
            query.append("WHERE ");
            appendFilters(relationshipFilters, "r0", query, properties);
        }
    }

    private static void createNodeMatchSubquery(Map<String, Object> properties, FiltersAtStartNode filtersAtStartNode,
        StringBuilder query, String nodeIdentifier) {
        String nodeLabel = filtersAtStartNode.startNodeLabel;
        List<Filter> nodeFilters = filtersAtStartNode.content;
        if (!(nodeLabel == null || nodeFilters.isEmpty())) {
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
