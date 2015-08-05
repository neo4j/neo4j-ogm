/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.session.request.strategy;


import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.cypher.query.GraphRowModelQuery;
import org.neo4j.ogm.cypher.query.Query;
import org.neo4j.ogm.session.Utils;

import java.util.*;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class VariableDepthQuery implements QueryStatements {

    @Override
    public Query findOne(Long id, int depth) {
        int max = max(depth);
        int min = min(max);
        if (depth < 0) {
            return InfiniteDepthReadStrategy.findOne(id);
        }
        if (max > 0) {
            String qry = String.format("MATCH (n) WHERE id(n) = { id } WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", min, max);
            return new GraphModelQuery(qry, Utils.map("id", id));
        } else {
            return DepthZeroReadStrategy.findOne(id);
        }
    }

    @Override
    public Query findAll(Collection<Long> ids, int depth) {
        int max = max(depth);
        int min = min(max);
        if (depth < 0) {
            return InfiniteDepthReadStrategy.findAll(ids);
        }
        if (max > 0) {
            String qry=String.format("MATCH (n) WHERE id(n) in { ids } WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", min, max);
            return new GraphModelQuery(qry, Utils.map("ids", ids));
        } else {
            return DepthZeroReadStrategy.findAll(ids);
        }
    }

    @Override
    public Query findAllByType(String label, Collection<Long> ids, int depth) {
        int max = max(depth);
        int min = min(max);
        if (depth < 0) {
            return InfiniteDepthReadStrategy.findAllByLabel(label, ids);
        }
        if (max > 0) {
            String qry=String.format("MATCH (n:`%s`) WHERE id(n) in { ids } WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", label, min, max);
            return new GraphModelQuery(qry, Utils.map("ids", ids));
        } else {
            return DepthZeroReadStrategy.findAllByLabel(label, ids);
        }
    }

    @Override
    public Query findAll() {
        return new GraphModelQuery("MATCH p=()-->() RETURN p", Utils.map());
    }

    @Override
    public Query findByType(String label, int depth) {
        int max = max(depth);
        int min = min(max);
        if (depth < 0) {
            return InfiniteDepthReadStrategy.findByLabel(label);
        }
        if (max > 0) {
            String qry = String.format("MATCH (n:`%s`) WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", label, min, max);
            return new GraphModelQuery(qry, Utils.map());
        } else {
            return DepthZeroReadStrategy.findByLabel(label);
        }
    }

    @Override
    public Query findByProperties(String label, Filters parameters, int depth) {
        int max = max(depth);
        int min = min(max);
        if (depth < 0) {
            return InfiniteDepthReadStrategy.findByProperties(label, parameters);
        }
        if (max > 0) {
            Map<String,Object> properties = new HashMap<>();
            StringBuilder query = constructQuery(label, parameters, properties);
            query.append(String.format("WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p, ID(n)",min,max));
            return new GraphRowModelQuery(query.toString(), properties);
        } else {
            return DepthZeroReadStrategy.findByProperties(label, parameters);
        }
    }

    private static StringBuilder constructQuery(String label, Filters filters, Map<String, Object> properties) {
        Map<String, StringBuilder> matchClauses = new LinkedHashMap<>(); //All individual MATCH classes, grouped by node label
        Map<String, String> matchClauseIdentifiers = new HashMap<>(); //Mapping of the node label to the identifier used in the query
        List<StringBuilder> relationshipClauses = new ArrayList<>(); //All relationship clauses
        int matchClauseId = 0;
        String nodeIdentifier="n";

        //Create a match required to support the node entity we're supposed to return
        createOrFetchMatchClause(label,nodeIdentifier,matchClauses);

        for (Filter filter : filters) {
            StringBuilder matchClause;
            if(filter.isNested()) {
                if(filter.getBooleanOperator().equals(BooleanOperator.OR)) {
                    throw new UnsupportedOperationException("OR is not supported for nested properties on an entity");
                }
                nodeIdentifier = "m" + matchClauseId; //Each nested filter produces a unique id for each type of node label
                if(filter.isNestedRelationshipEntity()) {
                    //There is no match clause for a relationship entity, instead, we append parameters to the relationship
                    matchClause = constructRelationshipClause(filter, nodeIdentifier);
                    matchClauses.put(filter.getRelationshipType(),matchClause);
                    nodeIdentifier = "r"; //TODO this implies support for querying by one relationship entity only
                }
                else {
                    if(matchClauseIdentifiers.containsKey(filter.getNestedEntityTypeLabel())) {
                        //Use the node identifier already created for nodes with this label
                        nodeIdentifier = matchClauseIdentifiers.get(filter.getNestedEntityTypeLabel());
                    }
                    else {
                        //This node identifier  has not been constructed yet, so do so and also construct its' relationship clause
                        matchClauseIdentifiers.put(filter.getNestedEntityTypeLabel(),nodeIdentifier);
                        relationshipClauses.add(constructRelationshipClause(filter, nodeIdentifier));
                    }
                    matchClause = createOrFetchMatchClause(filter.getNestedEntityTypeLabel(), nodeIdentifier, matchClauses);
                }
                matchClauseId++;
            }
            else {
                //If the filter is not nested, it belongs to the node we're returning
                nodeIdentifier = "n";
                matchClause = createOrFetchMatchClause(label, nodeIdentifier, matchClauses);
            }
            appendFilter(filter, nodeIdentifier, matchClause, properties);
        }
        //Construct the query by appending all match clauses followed by all relationship clauses
        return buildQuery(matchClauses, relationshipClauses);
    }

    private static StringBuilder buildQuery(Map<String, StringBuilder> matchClauses, List<StringBuilder> relationshipClauses) {
        StringBuilder query = new StringBuilder();
        for(StringBuilder matchClause : matchClauses.values()) {
            query.append(matchClause);
        }
        for(StringBuilder relationshipClause : relationshipClauses) {
            query.append(relationshipClause);
        }
        return query;
    }

    /**
     * Append a filter to a query in the form of a parameter
     * @param filter the {@link Filter} to extract the parameter from
     * @param nodeIdentifier the node identifier that the parameter belongs to
     * @param query the query
     * @param properties property map containing the parameter name and value to bind to the query
     */
    private static void appendFilter(Filter filter, String nodeIdentifier, StringBuilder query, Map<String, Object> properties) {
        String uniquePropertyName = filter.getPropertyName();
        if(filter.isNested()) {
            //Nested entities may have the same property name, so we make them unique by qualifying them with the nested property name on the owning entity
            uniquePropertyName = filter.getNestedPropertyName() + "_" + filter.getPropertyName();
        }

        if(query.indexOf(" WHERE ") == -1) {
            query.append("WHERE ");
        }
        else {
            if (!filter.getBooleanOperator().equals(BooleanOperator.NONE)) {
                query.append(filter.getBooleanOperator().getValue()).append(" ");
            }
        }
        query.append(String.format("%s.`%s` %s { `%s` } ", nodeIdentifier, filter.getPropertyName(), filter.getComparisonOperator().getValue(), uniquePropertyName));
        properties.put(uniquePropertyName, filter.getPropertyValue());
    }

    /**
     * Construct a relationship match clause for a filter
     * @param filter the {@link Filter}
     * @param nodeIdentifier the node identifier used for the other node of the realtionship
     * @return the relationship clause
     */
    private static StringBuilder constructRelationshipClause(Filter filter, String nodeIdentifier) {
        StringBuilder relationshipMatch;
        relationshipMatch = new StringBuilder("MATCH (n)");
        if(filter.getRelationshipDirection().equals(Relationship.INCOMING)) {
			relationshipMatch.append("<");
		}
        relationshipMatch.append(String.format("-[%s:`%s`]-", filter.isNestedRelationshipEntity() ? "r" : "", filter.getRelationshipType()));
        if(filter.getRelationshipDirection().equals(Relationship.OUTGOING)) {
			relationshipMatch.append(">");
		}
        relationshipMatch.append(String.format("(%s) ", nodeIdentifier));
        return relationshipMatch;
    }

    /**
     * Create of fetch an existing match clause for a node with a given label and node identifier
     * @param label the label of the node
     * @param nodeIdentifier the node identifier
     * @param matchClauses Map of existing match clauses, with key=node label and value=match clause
     * @return the match clause
     */
    private static StringBuilder createOrFetchMatchClause(String label, String nodeIdentifier, Map<String,StringBuilder> matchClauses) {
        if(matchClauses.containsKey(label)) {
            return matchClauses.get(label);
        }
        StringBuilder matchClause = new StringBuilder();

        matchClause.append(String.format("MATCH (%s:`%s`) ", nodeIdentifier, label));
        matchClauses.put(label, matchClause);
        return matchClause;
    }

    private int min(int depth) {
        return Math.min(0, depth);
    }

    private int max(int depth) {
        return Math.max(0, depth);
    }

    private static class DepthZeroReadStrategy {

        public static GraphModelQuery findOne(Long id) {
            return new GraphModelQuery("MATCH (n) WHERE id(n) = { id } RETURN n", Utils.map("id", id));
        }

        public static GraphModelQuery findAll(Collection<Long> ids) {
            return new GraphModelQuery("MATCH (n) WHERE id(n) in { ids } RETURN n", Utils.map("ids", ids));
        }

        public static GraphModelQuery findAllByLabel(String label, Collection<Long> ids) {
            return new GraphModelQuery(String.format("MATCH (n:`%s`) WHERE id(n) in { ids } RETURN n",label), Utils.map("ids", ids));
        }


        public static GraphModelQuery findByLabel(String label) {
            return new GraphModelQuery(String.format("MATCH (n:`%s`) RETURN n", label), Utils.map());
        }

        public static GraphModelQuery findByProperties(String label, Filters parameters) {
            Map<String,Object> properties = new HashMap<>();
            StringBuilder query = constructQuery(label, parameters, properties);
            query.append("RETURN n");
            return new GraphModelQuery(query.toString(), properties);
        }

    }

    private static class InfiniteDepthReadStrategy {

        public static GraphModelQuery findOne(Long id) {
            return new GraphModelQuery("MATCH (n) WHERE id(n) = { id } WITH n MATCH p=(n)-[*0..]-(m) RETURN p", Utils.map("id", id));
        }

        public static GraphModelQuery findAll(Collection<Long> ids) {
            return new GraphModelQuery("MATCH (n) WHERE id(n) in { ids } WITH n MATCH p=(n)-[*0..]-(m) RETURN p", Utils.map("ids", ids));
        }

        public static GraphModelQuery findAllByLabel(String label, Collection<Long> ids) {
            return new GraphModelQuery(String.format("MATCH (n:`%s`) WHERE id(n) in { ids } WITH n MATCH p=(n)-[*0..]-(m) RETURN p",label), Utils.map("ids", ids));
        }

        public static GraphModelQuery findByLabel(String label) {
            return new GraphModelQuery(String.format("MATCH (n:`%s`) WITH n MATCH p=(n)-[*0..]-(m) RETURN p", label), Utils.map());
        }

        public static GraphRowModelQuery findByProperties(String label, Filters parameters) {
            Map<String,Object> properties = new HashMap<>();
            StringBuilder query = constructQuery(label, parameters, properties);
            query.append(" WITH n MATCH p=(n)-[*0..]-(m) RETURN p, ID(n)");
            return new GraphRowModelQuery(query.toString(), properties);
        }

    }
}
