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

package org.neo4j.ogm.cypher.compiler.builders.statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.cypher.compiler.CypherStatementBuilder;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.request.OptimisticLockingConfig;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;

/**
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class ExistingNodeStatementBuilder extends BaseBuilder implements CypherStatementBuilder {

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

            if (firstNode.hasVersionProperty()) {
                appendVersionPropertyCheck(queryBuilder, firstNode, "n");
            }

            Set<String> previousDynamicLabels = firstNode.getPreviousDynamicLabels();
            for (String label : previousDynamicLabels) {
                queryBuilder.append(String.format(" REMOVE n:`%s` ", label));
            }

            queryBuilder.append(firstNode.createPropertyRemovalFragment("n"));

            queryBuilder.append("SET n");
            for (String label : firstNode.getLabels()) {
                queryBuilder.append(":`").append(label).append("`");
            }

            queryBuilder.append(" SET n += row.props RETURN row.nodeId as ref, ID(n) as id, {type} as type");
            List<Map> rows = new ArrayList<>();
            for (Node node : existingNodes) {
                Map<String, Object> rowMap = new HashMap<>();
                rowMap.put("nodeId", node.getId());
                Map<String, Object> props = new HashMap<>();
                for (Property property : node.getPropertyList()) {
                    // Don't include version property into props, it will be incremented by the query
                    if (!property.equals(node.getVersion())) {
                        props.put((String) property.getKey(), property.getValue());
                    }
                }
                rowMap.put("props", props);
                if (node.hasVersionProperty()) {
                    Property version = node.getVersion();
                    rowMap.put((String) version.getKey(), version.getValue());
                }
                rows.add(rowMap);
            }
            parameters.put("type", "node");
            parameters.put("rows", rows);

            if (firstNode.hasVersionProperty()) {
                OptimisticLockingConfig olConfig = new OptimisticLockingConfig(rows.size(),
                    firstNode.getLabels(), firstNode.getVersion().getKey());
                return statementFactory.statement(queryBuilder.toString(), parameters, olConfig);
            }
        }

        return statementFactory.statement(queryBuilder.toString(), parameters);
    }
}
