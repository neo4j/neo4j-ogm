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
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;

/**
 * @author Luanne Misquitta
 * @author Mark Angrish
 */
public class ExistingRelationshipStatementBuilder implements CypherStatementBuilder {

    private final StatementFactory statementFactory;

    private final Set<Edge> edges;

    public ExistingRelationshipStatementBuilder(Set<Edge> edges, StatementFactory statementFactory) {
        this.edges = edges;
        this.statementFactory = statementFactory;
    }


    @Override
    public Statement build() {
        final Map<String, Object> parameters = new HashMap<>();
        final StringBuilder queryBuilder = new StringBuilder();

        if (edges.size() > 0) {
            //queryBuilder.append("START r=rels({relIds}) SET r += ({rows}[toString(id(r))]).props"); //TODO 2.3+
            queryBuilder.append("START r=rel({relIds}) FOREACH (row in filter(row in {rows} where row.relId = id(r)) | SET r += row.props) ");
            queryBuilder.append("RETURN ID(r) as ref, ID(r) as id, {type} as type");
            List<Long> relIds = new ArrayList<>(edges.size());
            List<Map> rows = new ArrayList<>();
            for (Edge edge : edges) {
                Map<String, Object> rowMap = new HashMap<>();
                rowMap.put("relId", edge.getId());
                Map<String, Object> props = new HashMap<>();
                for (Property property : edge.getPropertyList()) {
                    props.put((String) property.getKey(), property.getValue());
                }
                rowMap.put("props", props);
                rows.add(rowMap);
                relIds.add(edge.getId());
            }
            parameters.put("rows", rows);
            parameters.put("relIds", relIds);
            parameters.put("type", "rel");
        }

        return statementFactory.statement(queryBuilder.toString(), parameters);
    }
}
