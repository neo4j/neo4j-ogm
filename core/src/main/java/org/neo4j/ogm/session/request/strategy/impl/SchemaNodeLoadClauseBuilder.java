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

package org.neo4j.ogm.session.request.strategy.impl;

import org.neo4j.ogm.metadata.schema.Node;
import org.neo4j.ogm.metadata.schema.Schema;
import org.neo4j.ogm.session.request.strategy.LoadClauseBuilder;

/**
 * Schema based load clause builder for nodes - starts from given node variable
 *
 * @author Frantisek Hartman
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

        Node node = schema.findNode(label);
        expand(sb, variable, node, depth);

        return sb.toString();
    }

}
