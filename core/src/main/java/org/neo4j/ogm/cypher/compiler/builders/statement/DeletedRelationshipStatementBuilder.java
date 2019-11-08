/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.cypher.compiler.builders.statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.cypher.compiler.CypherStatementBuilder;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;

/**
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Michael J. Simons
 */
public class DeletedRelationshipStatementBuilder implements CypherStatementBuilder {

    private final StatementFactory statementFactory;

    private final Set<Edge> deletedEdges;

    public DeletedRelationshipStatementBuilder(Set<Edge> deletedEdges, StatementFactory statementFactory) {
        this.deletedEdges = deletedEdges;
        this.statementFactory = statementFactory;
    }

    @Override
    public Statement build() {
        final Map<String, Object> parameters = new HashMap<>();
        final StringBuilder queryBuilder = new StringBuilder();

        if (deletedEdges != null && deletedEdges.size() > 0) {
            Edge firstEdge = deletedEdges.iterator().next();

            queryBuilder.append("UNWIND $rows as row ")
                .append("MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode ")
                .append("MATCH (endNode) WHERE ID(endNode) = row.endNodeId ")
                .append("MATCH (startNode)-[rel:`").append(firstEdge.getType()).append("`]->(endNode) ")
                .append("DELETE rel");

            List<Map> rows = new ArrayList<>();
            for (Edge edge : deletedEdges) {
                Map<String, Object> rowMap = new HashMap<>();
                rowMap.put("startNodeId", edge.getStartNode());
                rowMap.put("endNodeId", edge.getEndNode());
                rows.add(rowMap);
            }
            parameters.put("rows", rows);
        }

        return statementFactory.statement(queryBuilder.toString(), parameters);
    }
}
