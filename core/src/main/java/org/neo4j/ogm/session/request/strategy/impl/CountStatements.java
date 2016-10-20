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

package org.neo4j.ogm.session.request.strategy.impl;


import java.util.Collections;

import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.request.FilteredQuery;
import org.neo4j.ogm.session.request.FilteredQueryBuilder;
import org.neo4j.ogm.session.request.strategy.AggregateStatements;

/**
 * Encapsulates Cypher statements used to execute aggregation queries.
 *
 * @author Adam George
 * @author Vince Bickers
 */
public class CountStatements implements AggregateStatements {

    @Override
    public CypherQuery countNodes() {
        return new DefaultRowModelRequest("MATCH (n) RETURN COUNT(n)", Utils.map());
    }

    @Override
    public CypherQuery countNodes(String label) {
        return countNodes(Collections.singletonList(label));
    }

    @Override
    public CypherQuery countNodes(Iterable<String> labels) {
        StringBuilder cypherLabels = new StringBuilder();
        for (String label : labels) {
            cypherLabels.append(":`").append(label).append('`');
        }
        return new DefaultRowModelRequest(String.format("MATCH (n%s) RETURN COUNT(n)", cypherLabels.toString()),
                Collections.<String, String> emptyMap());
    }

    @Override
    public CypherQuery countNodes(String label, Filters filters) {
        FilteredQuery query = FilteredQueryBuilder.buildNodeQuery(label, filters);
        query.setReturnClause(" RETURN COUNT(n)");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }

    @Override
    public CypherQuery countEdges() {
        return new DefaultRowModelRequest("MATCH (n)-[r]->() RETURN COUNT(r)", Utils.map());
    }

    @Override
    public CypherQuery countEdges(String type) {
        return new DefaultRowModelRequest(String.format("MATCH (n)-[r:`%s`]->() RETURN COUNT(r)", type), Utils.map());
    }

    @Override
    public CypherQuery countEdges(String type, Filters filters) {
        FilteredQuery query = FilteredQueryBuilder.buildRelationshipQuery(type, filters);
        query.setReturnClause(" RETURN COUNT(n)");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }

    @Override
    public CypherQuery countEdges(String startLabel, String type, String endLabel) {
        return new DefaultRowModelRequest(String.format("MATCH (:`%s`)-[r:`%s`]->(:`%s`) RETURN COUNT(r)", startLabel, type, endLabel), Utils.map());
    }
}
