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
import org.neo4j.ogm.metadata.schema.Schema;
import org.neo4j.ogm.session.request.strategy.LoadClauseBuilder;

/**
 * Schema based load clause builder for nodes - starts from given node variable
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class SchemaNodeLoadClauseBuilder extends AbstractSchemaLoadClauseBuilder implements LoadClauseBuilder {

    public SchemaNodeLoadClauseBuilder(Schema schema) {
        super(schema);
    }

    public String build(String variable, String label, int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Only queries with depth >= 0 can be built, depth=" + depth);
        }

        StringBuilder sb = new StringBuilder();

        newLine(sb);

        sb.append(" RETURN ");
        newLine(sb);
        sb.append(variable);
        newLine(sb);

        Node node;
        final int separatorIndex = label.indexOf("`:`");
        if (separatorIndex < 0) {
            node = schema.findNode(label);
        } else {
            node = schema.findNode(label.substring(0, separatorIndex));
        }

        expand(sb, variable, node, depth);

        return sb.toString();
    }

}
