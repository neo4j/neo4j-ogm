/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.session.request.strategy;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.*;
import org.neo4j.ogm.exception.InvalidDepthException;
import org.neo4j.ogm.session.Utils;

import java.util.*;

/**
 * @author Luanne Misquitta
 */
public class VariableDepthRelationshipQuery implements QueryStatements {

    @Override
    public Query findOne(Long id, int depth) {
        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            String qry = String.format("MATCH (n)-[r]->() WHERE ID(r) = { id } WITH n,r MATCH p=(n)-[*%d..%d]-(m) RETURN collect(distinct p)", min, max);
            return new GraphModelQuery(qry, Utils.map("id", id));
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    @Override
    public Query findAll(Collection<Long> ids, int depth) {
        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            String qry=String.format("MATCH (n)-[r]->() WHERE ID(r) IN { ids } WITH r,n MATCH p=(n)-[*%d..%d]-(m) RETURN collect(distinct p)", min, max);
            return new GraphModelQuery(qry, Utils.map("ids", ids));
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    @Override
    public Query findAll() {
        return new GraphModelQuery("MATCH p=()-->() RETURN p", Utils.map());
    }

    @Override
    public Query findByType(String type, int depth) {
        int max = max(depth);
        if (max > 0) {
            String qry = String.format("MATCH p=()-[r:`%s`*..%d]-() RETURN collect(distinct p)", type, max);
            return new GraphModelQuery(qry, Utils.map());
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }


	@Override
	public Query findByProperties(String type, Filters parameters, int depth) {
		int max = max(depth);
		int min = min(max);
		if (max > 0) {
			Map<String, Object> properties = new HashMap<>();
            StringBuilder query = constructQuery(type, parameters, properties);
			query.append(String.format("WITH r,n MATCH p=(n)-[*%d..%d]-() RETURN collect(distinct p), ID(r)", min, max));
			return new GraphRowModelQuery(query.toString(), properties);
		} else {
			throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
		}
	}

    private static StringBuilder constructQuery(String type, Filters filters, Map<String, Object> properties) {
        List<Filter> startNodeFilters = new ArrayList<>(); //All filters that apply to the start node
        List<Filter> endNodeFilters = new ArrayList<>(); //All filters that apply to the end node
        List<Filter> relationshipFilters = new ArrayList<>(); //All filters that apply to the relationship
        String startNodeLabel = null;
        String endNodeLabel = null;

        for(Filter filter : filters) {
            if(filter.isNested()) {
                if(filter.getBooleanOperator().equals(BooleanOperator.OR)) {
                    throw new UnsupportedOperationException("OR is not supported for nested properties on a relationship entity");
                }
                if(filter.getRelationshipDirection().equals(Relationship.OUTGOING)) {
                    if(startNodeLabel==null) {
                        startNodeLabel = filter.getNestedEntityTypeLabel();
                        filter.setBooleanOperator(BooleanOperator.NONE); //the first filter for the start node
                    }
                    startNodeFilters.add(filter);
                }
                else {
                    if(endNodeLabel==null) {
                        endNodeLabel = filter.getNestedEntityTypeLabel();
                        filter.setBooleanOperator(BooleanOperator.NONE); //the first filter for the end node
                    }
                    endNodeFilters.add(filter);
                }
            }
            else {
                if(relationshipFilters.size()==0) {
                    filter.setBooleanOperator(BooleanOperator.NONE); //TODO think about the importance of the first filter and stop using this as a condition to test against
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
        if(relationshipFilters.size() > 0) {
            query.append("WHERE ");
            appendFilters(relationshipFilters,"r",query, properties);
        }
    }

    private static void createNodeMatchSubquery(Map<String, Object> properties, List<Filter> nodeFilters, String nodeLabel, StringBuilder query, String nodeIdentifier) {
        if (nodeLabel != null) {
            query.append(String.format("MATCH (%s:`%s`) WHERE ", nodeIdentifier,nodeLabel));
            appendFilters(nodeFilters, nodeIdentifier, query, properties);
        }
    }

    private static void appendFilters(List<Filter> filters, String nodeIdentifier, StringBuilder query,  Map<String, Object> properties) {
        for(Filter filter : filters) {
            if(!filter.getBooleanOperator().equals(BooleanOperator.NONE)) {
                query.append(filter.getBooleanOperator().getValue()).append(" ");
            }
			query.append(String.format("%s.`%s` %s { `%s` } ",nodeIdentifier,filter.getPropertyName(), filter.getComparisonOperator().getValue(), filter.getPropertyName()));
            properties.put(filter.getPropertyName(),filter.getPropertyValue());
		}
    }

    private int min(int depth) {
        return Math.min(0, depth);
    }

    private int max(int depth) {
		return Math.max(0, depth);
	}
}
