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
package org.neo4j.ogm.session.request.strategy.impl;

import java.util.Collections;

import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.CypherQuery;
import org.neo4j.ogm.cypher.query.DefaultRowModelRequest;
import org.neo4j.ogm.session.request.FilteredQuery;
import org.neo4j.ogm.session.request.FilteredQueryBuilder;
import org.neo4j.ogm.session.request.strategy.AggregateStatements;

/**
 * Encapsulates Cypher statements used to execute aggregation queries.
 *
 * @author Adam George
 * @author Vince Bickers
 * @author Jasper Blues
 * @author Michael J. Simons
 */
public class CountStatements implements AggregateStatements {

    @Override
    public CypherQuery countNodes(Iterable<String> labels) {
        StringBuilder cypherLabels = new StringBuilder();
        for (String label : labels) {
            cypherLabels.append(":`").append(label).append('`');
        }
        return new DefaultRowModelRequest(String.format("MATCH (n%s) RETURN COUNT(n)", cypherLabels.toString()),
            Collections.<String, String>emptyMap());
    }

    @Override
    public CypherQuery countNodes(String label, Iterable<Filter> filters) {
        FilteredQuery query = FilteredQueryBuilder.buildNodeQuery(label, filters);
        query.setReturnClause(" RETURN COUNT(n)");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }

    @Override
    public CypherQuery countEdges(String type, Iterable<Filter> filters) {
        FilteredQuery query = FilteredQueryBuilder.buildRelationshipQuery(type, filters);
        query.setReturnClause(" RETURN COUNT(r0)");
        return new DefaultRowModelRequest(query.statement(), query.parameters());
    }

    @Override
    public CypherQuery countEdges(String startLabel, String type, String endLabel) {
        return new DefaultRowModelRequest(
            String.format("MATCH (:`%s`)-[r0:`%s`]->(:`%s`) RETURN COUNT(r0)", startLabel, type, endLabel),
            Collections.emptyMap());
    }
}
