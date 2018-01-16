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
