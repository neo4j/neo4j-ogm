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
package org.neo4j.ogm.session.request.strategy.impl;

import java.util.Collection;

import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.request.FilteredQuery;
import org.neo4j.ogm.session.request.FilteredQueryBuilder;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;

/**
 * @author Luanne Misquitta
 */
public class NodeDeleteStatements implements DeleteStatements {

    @Override

    public CypherQuery delete(Long id) {
        return new DefaultRowModelRequest("MATCH (n) WHERE ID(n) = { id } OPTIONAL MATCH (n)-[r0]-() DELETE r0, n",
            Utils.map("id", id));
    }

    @Override
    public CypherQuery delete(Collection<Long> ids) {
        return new DefaultRowModelRequest("MATCH (n) WHERE ID(n) in { ids } OPTIONAL MATCH (n)-[r0]-() DELETE r0, n",
            Utils.map("ids", ids));
    }

    @Override
    public CypherQuery deleteAll() {
        return new DefaultRowModelRequest("MATCH (n) OPTIONAL MATCH (n)-[r0]-() DELETE r0, n", Utils.map());
    }

    @Override
    public CypherQuery delete(String label) {
        return new DefaultRowModelRequest(
            String.format("MATCH (n:`%s`) OPTIONAL MATCH (n)-[r0]-() DELETE r0, n", label), Utils.map());
    }

    @Override
    public CypherQuery delete(String label, Iterable<Filter> filters) {
        FilteredQuery query = FilteredQueryBuilder.buildNodeQuery(label, filters);
        query.setReturnClause(" OPTIONAL MATCH (n)-[r0]-() DELETE r0, n");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }

    @Override
    public CypherQuery deleteAndList(String label, Iterable<Filter> filters) {
        FilteredQuery query = FilteredQueryBuilder.buildNodeQuery(label, filters);
        query.setReturnClause(" OPTIONAL MATCH (n)-[r0]-() DELETE r0, n RETURN ID(n)");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }
}
