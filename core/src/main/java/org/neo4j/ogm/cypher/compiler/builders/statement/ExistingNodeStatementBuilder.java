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

import static java.util.stream.Collectors.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.ogm.cypher.compiler.CypherStatementBuilder;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.request.OptimisticLockingConfig;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;

/**
 * @author Luanne Misquitta
 * @author Mark Angrish
 * @author Michael J. Simons
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

            queryBuilder
                .append("UNWIND $rows as row MATCH (n) WHERE ID(n)=row.nodeId ");

            if (firstNode.hasVersionProperty()) {
                queryBuilder.append(OptimisticLockingUtils.getFragmentForExistingNodesAndRelationships(firstNode, "n"));
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

            queryBuilder.append(" SET n += row.props RETURN row.nodeId as ref, ID(n) as id, $type as type");
            List<Map> rows = existingNodes.stream().map(node -> node.toRow("nodeId")).collect(toList());
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
