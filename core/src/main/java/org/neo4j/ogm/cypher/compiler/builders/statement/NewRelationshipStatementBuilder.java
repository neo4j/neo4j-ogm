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

package org.neo4j.ogm.cypher.compiler.builders.statement;

import static java.util.Collections.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.cypher.compiler.CypherStatementBuilder;
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;

/**
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class NewRelationshipStatementBuilder implements CypherStatementBuilder {

    private final StatementFactory statementFactory;

    private final Set<Edge> edges;

    public NewRelationshipStatementBuilder(Set<Edge> edges, StatementFactory statementFactory) {
        this.edges = edges;
        this.statementFactory = statementFactory;
    }

    @Override
    public Statement build() {

        final Map<String, Object> parameters = new HashMap<>();
        final StringBuilder queryBuilder = new StringBuilder();

        boolean hasProperties = false;
        if (edges != null && edges.size() > 0) {
            Edge firstEdge = edges.iterator().next();
            String relType = firstEdge.getType();
            hasProperties = firstEdge.getPropertyList().size() > 0;
            boolean hasPrimaryId = firstEdge.getPrimaryIdName() != null;

            queryBuilder.append("UNWIND {rows} as row ")
                .append("MATCH (startNode) WHERE ID(startNode) = row.startNodeId WITH row,startNode ")
                .append("MATCH (endNode) WHERE ID(endNode) = row.endNodeId ");

            if (hasProperties && !hasPrimaryId) {
                queryBuilder.append("CREATE ");
            } else {
                queryBuilder.append("MERGE ");
            }

            queryBuilder.append("(startNode)-[rel:`")
                .append(relType)
                .append("`");

            if (hasPrimaryId) {
                queryBuilder.append(" {`")
                    .append(firstEdge.getPrimaryIdName())
                    .append("`: row.props.`")
                    .append(firstEdge.getPrimaryIdName())
                    .append("`}");
            }
            queryBuilder.append("]->(endNode) ");

            if (hasProperties) {
                queryBuilder.append("SET rel += row.props ");

            }
            queryBuilder.append("RETURN row.relRef as ref, ID(rel) as id, row.type as type");

            List<Map> rows = new ArrayList<>();
            for (Edge edge : edges) {
                Map<String, Object> rowMap = new HashMap<>();
                rowMap.put("startNodeId", edge.getStartNode());
                rowMap.put("endNodeId", edge.getEndNode());
                rowMap.put("relRef", edge.getId());
                rowMap.put("type", "rel");
                if (hasProperties) {
                    Map<String, Object> props = new HashMap<>();
                    for (Property property : edge.getPropertyList()) {
                        props.put((String) property.getKey(), property.getValue());
                    }
                    rowMap.put("props", props);
                } else {
                    // need to put empty map here for simple relationships to avoid Cypher error because
                    // props is used in the query
                    rowMap.put("props", emptyMap());
                }
                rows.add(rowMap);
            }
            parameters.put("rows", rows);
        }

        return statementFactory.statement(queryBuilder.toString(), parameters);
    }
}
