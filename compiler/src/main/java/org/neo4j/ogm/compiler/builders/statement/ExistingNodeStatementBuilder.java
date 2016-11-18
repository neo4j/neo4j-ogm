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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.compiler.CypherStatementBuilder;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;

/**
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class ExistingNodeStatementBuilder implements CypherStatementBuilder {

    private final StatementFactory statementFactory;

    private final Set<Node> existingNodes;

    public ExistingNodeStatementBuilder(Set<Node> existingNodes, StatementFactory statementFactory) {
        this.existingNodes = existingNodes;
        this.statementFactory = statementFactory;
    }

    @Override
    public Statement build() {

        final Map<String, Object> parameters = new HashMap<>();
        final StringBuilder queryBuilder = new StringBuilder();

        if (existingNodes != null && existingNodes.size() > 0) {
            Node firstNode = existingNodes.iterator().next();

            queryBuilder.append("UNWIND {rows} as row ")
                    .append("MATCH (n) WHERE ID(n)=row.nodeId ");

            String[] removedLabels = firstNode.getRemovedLabels();
            if (removedLabels != null && removedLabels.length > 0) {
                for (String label : removedLabels) {
                    queryBuilder.append(String.format(" REMOVE n:`%s` ", label));
                }
            }

            queryBuilder.append("SET n");
            for (String label : firstNode.getLabels()) {
                queryBuilder.append(":`").append(label).append("`");
            }

            queryBuilder.append(" SET n += row.props RETURN row.nodeId as ref, ID(n) as id, row.type as type");
            List<Map> rows = new ArrayList<>();
            for (Node node : existingNodes) {
                Map<String, Object> rowMap = new HashMap<>();
                rowMap.put("nodeId", node.getId());
                rowMap.put("type", "node");
                Map<String, Object> props = new HashMap<>();
                for (Property property : node.getPropertyList()) {
                    props.put((String) property.getKey(), property.getValue());
                }
                rowMap.put("props", props);
                rows.add(rowMap);
            }
            parameters.put("rows", rows);
        }

        return statementFactory.statement(queryBuilder.toString(), parameters);
    }
}
