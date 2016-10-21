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


import java.util.Collection;

import org.neo4j.ogm.cypher.Filters;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.request.FilteredQuery;
import org.neo4j.ogm.session.request.FilteredQueryBuilder;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;

/**
 * @author Luanne Misquitta
 */
public class RelationshipDeleteStatements implements DeleteStatements {

    public CypherQuery delete(Long id) {
        return new DefaultRowModelRequest("MATCH (n)-[r]->() WHERE ID(r) = { id } DELETE r", Utils.map("id", id));
    }

    public CypherQuery delete(Collection<Long> ids) {
        return new DefaultRowModelRequest("MATCH (n)-[r]->() WHERE ID(r) IN { ids } DELETE r", Utils.map("ids", ids));
    }

    public CypherQuery deleteAll() {
        return new DefaultRowModelRequest("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r", Utils.map());
    }

    @Override
    public CypherQuery deleteAllAndCount() {
        return new DefaultRowModelRequest("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r RETURN COUNT(r)", Utils.map());
    }

    @Override
    public CypherQuery deleteAllAndList() {
        return new DefaultRowModelRequest("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE r RETURN ID(r)", Utils.map());
    }

    public CypherQuery delete(String type) {
        return new DefaultRowModelRequest(String.format("MATCH (n)-[r:`%s`]-() DELETE r", type), Utils.map());
    }

    @Override
    public CypherQuery deleteAndCount(String type) {
        return new DefaultRowModelRequest(String.format("MATCH (n)-[r:`%s`]-() DELETE r RETURN COUNT(r)", type), Utils.map());
    }

    @Override
    public CypherQuery deleteAndList(String type) {
        return new DefaultRowModelRequest(String.format("MATCH (n)-[r:`%s`]-() DELETE r RETURN ID(r)", type), Utils.map());
    }

    @Override
    public CypherQuery delete(String type, Filters filters) {
        FilteredQuery query = FilteredQueryBuilder.buildNodeQuery(type, filters);
        query.setReturnClause(" DELETE r");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }

    @Override
    public CypherQuery deleteAndCount(String type, Filters filters) {
        FilteredQuery query = FilteredQueryBuilder.buildNodeQuery(type, filters);
        query.setReturnClause(" DELETE r RETURN COUNT(r)");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }

    @Override
    public CypherQuery deleteAndList(String type, Filters filters) {
        FilteredQuery query = FilteredQueryBuilder.buildNodeQuery(type, filters);
        query.setReturnClause(" DELETE r RETURN ID(r)");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }
}
