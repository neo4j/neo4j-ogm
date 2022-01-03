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

import org.neo4j.ogm.metadata.schema.Node;
import org.neo4j.ogm.metadata.schema.Relationship;
import org.neo4j.ogm.metadata.schema.Schema;
import org.neo4j.ogm.session.request.strategy.LoadClauseBuilder;

/**
 * @author Frantisek Hartman
 */
public class SchemaRelationshipLoadClauseBuilder extends AbstractSchemaLoadClauseBuilder implements LoadClauseBuilder {


    public SchemaRelationshipLoadClauseBuilder(Schema schema) {
        super(schema);
    }

    @Override
    public String build(String label, int depth) {
        return build("r0", label, depth);
    }

    @Override
    public String build(String variable, String label, int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("Only positive depth parameter supported, depth = " + depth);
        }

        StringBuilder sb = new StringBuilder();

        newLine(sb);

        sb.append(" RETURN ");
        newLine(sb);
        sb.append(variable);
        newLine(sb);

        Relationship relationship = schema.findRelationship(label);

        // one step is going from r to start and end node so pass depth - 1
        sb.append(",n");
        Node start = relationship.start();
        expand(sb, "n", start, depth);
        sb.append(",m");
        Node end = relationship.other(start);
        expand(sb, "m", end, depth);

        return sb.toString();
    }

}
