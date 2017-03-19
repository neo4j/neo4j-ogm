/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
 * @author Jasper Blues
 */
public class RelationshipDeleteStatements implements DeleteStatements {

    public CypherQuery delete(Long id) {
        return new DefaultRowModelRequest("MATCH (n)-[r0]->() WHERE ID(r0) = { id } DELETE r0", Utils.map("id", id));
    }

    public CypherQuery delete(Collection<Long> ids) {
        return new DefaultRowModelRequest("MATCH (n)-[r0]->() WHERE ID(r0) IN { ids } DELETE r0", Utils.map("ids", ids));
    }

    public CypherQuery deleteAll() {
        return new DefaultRowModelRequest("MATCH (n) OPTIONAL MATCH (n)-[r0]-() DELETE r0", Utils.map());
    }

    @Override
    public CypherQuery deleteAllAndCount() {
        return new DefaultRowModelRequest("MATCH (n) OPTIONAL MATCH (n)-[r0]-() DELETE r0 RETURN COUNT(r0)", Utils.map());
    }

    @Override
    public CypherQuery deleteAllAndList() {
        return new DefaultRowModelRequest("MATCH (n) OPTIONAL MATCH (n)-[r0]-() DELETE r0 RETURN ID(r0)", Utils.map());
    }

    public CypherQuery delete(String type) {
        return new DefaultRowModelRequest(String.format("MATCH (n)-[r0:`%s`]-() DELETE r0", type), Utils.map());
    }

    @Override
    public CypherQuery deleteAndCount(String type) {
        return new DefaultRowModelRequest(String.format("MATCH (n)-[r0:`%s`]-() DELETE r0 RETURN COUNT(r0)", type), Utils.map());
    }

    @Override
    public CypherQuery deleteAndList(String type) {
        return new DefaultRowModelRequest(String.format("MATCH (n)-[r0:`%s`]-() DELETE r0 RETURN ID(r0)", type), Utils.map());
    }

    @Override
    public CypherQuery delete(String type, Iterable<Filter> filters) {
        FilteredQuery query = FilteredQueryBuilder.buildRelationshipQuery(type, filters);
        query.setReturnClause(" DELETE r0");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }

    @Override
    public CypherQuery deleteAndCount(String type, Iterable<Filter> filters) {
        FilteredQuery query = FilteredQueryBuilder.buildRelationshipQuery(type, filters);
        query.setReturnClause(" DELETE r0 RETURN COUNT(r0)");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }

    @Override
    public CypherQuery deleteAndList(String type, Iterable<Filter> filters) {
        FilteredQuery query = FilteredQueryBuilder.buildRelationshipQuery(type, filters);
        query.setReturnClause(" DELETE r0 RETURN ID(r0)");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }
}
