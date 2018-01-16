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

import static org.neo4j.ogm.annotation.Relationship.*;

import java.util.Map;

import org.neo4j.ogm.metadata.schema.Node;
import org.neo4j.ogm.metadata.schema.Relationship;
import org.neo4j.ogm.metadata.schema.Schema;

/**
 * Base class for schema based LoadClauseBuilder implementations
 *
 * @author Frantisek Hartman
 */
public abstract class AbstractSchemaLoadClauseBuilder {

    protected final Schema schema;

    protected final boolean pretty;

    public AbstractSchemaLoadClauseBuilder(Schema schema) {
        this.pretty = false;
        this.schema = schema;
    }

    protected void expand(StringBuilder sb, String variable, Node node, int depth) {
        if (depth > 0) {
            if (node.relationships().size() > 0) {
                sb.append(",[ ");

            }
            expand(sb, variable, node, 1, depth - 1);
            if (node.relationships().size() > 0) {
                sb.append(" ]");
            }
        }
    }

    protected void expand(StringBuilder sb, String variable, Node node, int level, int depth) {
        for (Map.Entry<String, Relationship> entry : node.relationships().entrySet()) {
            if (needsSeparator(sb)) {
                sb.append(", ");
            }

            listComprehension(sb, variable, entry.getValue(), node, level, depth);

        }

    }

    private boolean needsSeparator(StringBuilder sb) {
        for (int i = sb.length() - 1; i >= 0; i--) {
            char ch = sb.charAt(i);
            if (!Character.isWhitespace(ch)) {
                return ch != '[' && ch != ',';
            }
        }
        return false;
    }

    private void listComprehension(StringBuilder sb, String fromNodeVar, Relationship relationship, Node node,
        int level, int depth) {

        String direction = relationship.direction(node);
        Node toNode = relationship.other(node);

        String relVar = relVariableName(relationship, level);
        String toNodeVar = variableName(toNode, level);

        sb.append("[ (");
        sb.append(fromNodeVar);
        sb.append(")");
        switch (direction) {
            case INCOMING:
                appendRel(sb, relVar, relationship.type(), "<-[", "]-");
                break;

            case OUTGOING:
                appendRel(sb, relVar, relationship.type(), "-[", "]->");
                break;

            default:
                appendRel(sb, relVar, relationship.type(), "-[", "]-");
                break;
        }

        sb.append("(");
        sb.append(toNodeVar);
        if (toNode.label().isPresent()) {
            sb.append(":`");
            sb.append(toNode.label().get());
            sb.append("`");
        }
        sb.append(") | [ ");
        sb.append(relVar);
        sb.append(", ");
        sb.append(toNodeVar);

        if (depth > 0 && !toNode.relationships().isEmpty()) {
            sb.append(", [ ");
            expand(sb, toNodeVar, toNode, level + 1, depth - 1);
            sb.append(" ]");
        }

        sb.append(" ] ]");
    }

    private String relVariableName(Relationship relationship, int level) {
        return "r_" + Character.toLowerCase(relationship.type().charAt(0)) + level;
    }

    private String variableName(Node node, int level) {
        String label = node.label().orElse("x");
        char name = label.charAt(0);
        return "" + Character.toLowerCase(name) + level;
    }

    private void appendRel(StringBuilder sb, String variable, String type, String start, String end) {
        sb.append(start)
            .append(variable)
            .append(":`")
            .append(type)
            .append("`")
            .append(end);
    }

    protected void newLine(StringBuilder sb) {
        if (pretty) {
            sb.append("\n");
        }
    }
}
