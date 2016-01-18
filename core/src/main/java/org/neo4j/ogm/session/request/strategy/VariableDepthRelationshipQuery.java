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

import java.util.*;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.cypher.BooleanOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.AbstractRequest;
import org.neo4j.ogm.cypher.query.DefaultGraphModelRequest;
import org.neo4j.ogm.cypher.query.DefaultGraphRowListModelRequest;
import org.neo4j.ogm.exception.InvalidDepthException;
import org.neo4j.ogm.exception.MissingOperatorException;
import org.neo4j.ogm.session.Utils;

/**
 * @author Luanne Misquitta
 */
public class VariableDepthRelationshipQuery implements QueryStatements {

    @Override
    public AbstractRequest findOne(Long id, int depth) {
        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            String qry = String.format("MATCH (n)-[r]->() WHERE ID(r) = { id } WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", min, max);
            return new DefaultGraphModelRequest(qry, Utils.map("id", id));
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    @Override
    public AbstractRequest findAll(Collection<Long> ids, int depth) {
        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            String qry=String.format("MATCH (n)-[r]->() WHERE ID(r) IN { ids } WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", min, max);
            return new DefaultGraphModelRequest(qry, Utils.map("ids", ids));
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    @Override
    public AbstractRequest findAllByType(String type, Collection<Long> ids, int depth) {
        int max = max(depth);
        int min = min(max);
        if (max > 0) {
            String qry=String.format("MATCH (n)-[r:`%s`]->() WHERE ID(r) IN { ids } WITH n MATCH p=(n)-[*%d..%d]-(m) RETURN p", type, min, max);
            return new DefaultGraphModelRequest(qry, Utils.map("ids", ids));
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }

    @Override
    public AbstractRequest findAll() {
        return new DefaultGraphModelRequest("MATCH p=()-->() RETURN p", Utils.map());
    }

    @Override
    public AbstractRequest findByType(String type, int depth) {
        int max = max(depth);
        if (max > 0) {
            String qry = String.format("MATCH p=()-[r:`%s`*..%d]-() RETURN p", type, max);
            return new DefaultGraphModelRequest(qry, Utils.map());
        } else {
            throw new InvalidDepthException("Cannot load a relationship entity with depth 0 i.e. no start or end node");
        }
    }


	@Override
	public AbstractRequest findByProperties(String type, Filters parameters, int depth) {
		int max = max(depth);
		int min = min(max);
		if (max > 0) {
			Map<String, Object> properties = new HashMap<>();
            StringBuilder query = constructQuery(type, parameters, properties);
			query.append(String.format("WITH n,r MATCH p=(n)-[*%d..%d]-() RETURN p, ID(r)", min, max));
			return new DefaultGraphRowListModelRequest(query.toString(), properties);
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
        boolean noneOperatorEncounteredInStartFilters = false;
        boolean noneOperatorEncounteredInEndFilters = false;

        for(Filter filter : filters) {
            if(filter.isNested()) {
                if(filter.getBooleanOperator().equals(BooleanOperator.OR)) {
                    throw new UnsupportedOperationException("OR is not supported for nested properties on a relationship entity");
                }
                if(filter.getRelationshipDirection().equals(Relationship.OUTGOING)) {
                    if (filter.getBooleanOperator().equals(BooleanOperator.NONE)) {
                        if (noneOperatorEncounteredInStartFilters) {
                            throw new MissingOperatorException("BooleanOperator missing for filter with property name " + filter.getPropertyName());
                        }
                        noneOperatorEncounteredInStartFilters = true;
                    }
                    if(startNodeLabel==null) {
                        startNodeLabel = filter.getNestedEntityTypeLabel();
                        filter.setBooleanOperator(BooleanOperator.NONE); //the first filter for the start node
                    }
                    startNodeFilters.add(filter);
                }
                else {
                    if (filter.getBooleanOperator().equals(BooleanOperator.NONE)) {
                        if (noneOperatorEncounteredInEndFilters) {
                            throw new MissingOperatorException("BooleanOperator missing for filter with property name " + filter.getPropertyName());
                        }
                        noneOperatorEncounteredInEndFilters = true;
                    }
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
            String uniquePropertyName = filter.getPropertyName();
            if(filter.isNested()) {
                //Nested entities may have the same property name, so we make them unique by qualifying them with the nested property name on the owning entity
                uniquePropertyName = filter.getNestedPropertyName() + "_" + filter.getPropertyName();
            }
            String propertyExpressionPattern = filter.isNegated()
                    ? "NOT(%s.`%s` %s { `%s` }) "
                    : "%s.`%s` %s { `%s` } ";
            query.append(String.format(propertyExpressionPattern, nodeIdentifier, filter.getPropertyName(), filter.getComparisonOperator().getValue(), uniquePropertyName));
            properties.put(uniquePropertyName, filter.getTransformedPropertyValue());
		}
    }

    private int min(int depth) {
        return Math.min(0, depth);
    }

    private int max(int depth) {
		return Math.max(0, depth);
	}
}
