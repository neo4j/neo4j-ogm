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
