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

package org.neo4j.ogm.cypher.compiler.builders.statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.cypher.compiler.CypherStatementBuilder;
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
