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

package org.neo4j.ogm.compiler.builders.statement;

import java.util.*;

import org.neo4j.ogm.compiler.CypherStatementBuilder;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;

/**
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class NewNodeStatementBuilder implements CypherStatementBuilder {

    private final StatementFactory statementFactory;

    private final Set<Node> newNodes;

    public NewNodeStatementBuilder(Set<Node> newNodes, StatementFactory statementFactory) {
        this.newNodes = newNodes;
        this.statementFactory = statementFactory;
    }

    @Override
    public Statement build() {

        final Map<String, Object> parameters = new HashMap<>();
        final StringBuilder queryBuilder = new StringBuilder();

        if (newNodes != null && newNodes.size() > 0) {
            Node firstNode = newNodes.iterator().next();

            queryBuilder.append("UNWIND {rows} as row ");

            if (firstNode.getPrimaryIndex() != null) {
                queryBuilder.append("MERGE (n");
            } else {
                queryBuilder.append("CREATE (n");
            }

            for (String label : firstNode.getLabels()) {
                queryBuilder.append(":`").append(label).append("`");
            }

            if (firstNode.getPrimaryIndex() != null) {
                queryBuilder.append("{")
                        .append(firstNode.getPrimaryIndex())
                        .append(": row.props.")
                        .append(firstNode.getPrimaryIndex())
                        .append("}");
            }

            queryBuilder.append(") SET n=row.props RETURN row.nodeRef as ref, ID(n) as id, row.type as type");
            List<Map> rows = new ArrayList<>();
            for (Node node : newNodes) {
                Map<String, Object> rowMap = new HashMap<>();
                rowMap.put("nodeRef", node.getId());
                rowMap.put("type", "node");
                Map<String, Object> props = new HashMap<>();
                for (Property property : node.getPropertyList()) {
                    if (property.getValue() != null) {
                        props.put((String) property.getKey(), property.getValue());
                    }
                }
                rowMap.put("props", props);
                rows.add(rowMap);
            }
            parameters.put("rows", rows);
        }

        return statementFactory.statement(queryBuilder.toString(), parameters);
    }
}
