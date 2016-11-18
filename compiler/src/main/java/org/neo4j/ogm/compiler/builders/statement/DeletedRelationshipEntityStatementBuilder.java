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
import org.neo4j.ogm.model.Edge;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;

/**
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class DeletedRelationshipEntityStatementBuilder implements CypherStatementBuilder {

    private final StatementFactory statementFactory;

    private final Set<Edge> deletedEdges;

    public DeletedRelationshipEntityStatementBuilder(Set<Edge> deletedEdges, StatementFactory statementFactory) {
        this.deletedEdges = deletedEdges;
        this.statementFactory = statementFactory;
    }

    @Override
    public Statement build() {

        final Map<String, Object> parameters = new HashMap<>();
        final StringBuilder queryBuilder = new StringBuilder();

        if (deletedEdges != null && deletedEdges.size() > 0) {

            queryBuilder.append("START r=rel({relIds}) DELETE r");

            List<Long> relIds = new ArrayList<>(deletedEdges.size());
            List<Map> rows = new ArrayList<>();
            for (Edge edge : deletedEdges) {
                Map<String, Object> rowMap = new HashMap<>();
                rowMap.put("relId", edge.getId());
                rows.add(rowMap);
                relIds.add(edge.getId());
            }
            parameters.put("rows", rows);
            parameters.put("relIds", relIds);
        }
        return statementFactory.statement(queryBuilder.toString(), parameters);
    }
}
