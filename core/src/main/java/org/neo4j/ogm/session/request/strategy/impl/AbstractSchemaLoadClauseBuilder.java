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
package org.neo4j.ogm.session.request.strategy.impl;

import static org.neo4j.ogm.annotation.Relationship.*;

import java.util.Collection;
import java.util.stream.Collectors;

import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.reflect.EntityAccessManager;
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
            Collection<Relationship> relationships = getRelevantRelationships(node);
            if (relationships.size() > 0) {
                sb.append(",[ ");

            }
            expand(sb, variable, node, relationships, 1, depth - 1);
            if (relationships.size() > 0) {
                sb.append(" ]");
            }
        }
    }

    protected Collection<Relationship> getRelevantRelationships(Node node) {
        return node.relationships().values();
    }

    protected Collection<Relationship> getNonLazyLoadingRelationships(MetaData metaData, Node node) {
        Collection<Relationship> relationships = node.relationships().values();
        return node.label()
            .map(metaData::classInfo)
            .<Collection<Relationship>>map(classInfo -> relationships
                .stream()
                .filter(relationship -> doesNotSupportLazyLoading(classInfo, relationship, metaData, node))
                .collect(Collectors.toList()))
            .orElse(relationships);
    }

    private Boolean doesNotSupportLazyLoading(ClassInfo info,
        Relationship relationship,
        MetaData metaData,
        Node node) {

        ClassInfo relationType = metaData.classInfo(relationship.type());
        FieldInfo fieldInfo = null;
        if (relationType == null) {
            relationType = relationship.other(node)
                .label()
                .map(metaData::classInfo)
                .orElse(null);
        }
        if (relationType != null) {
            fieldInfo = EntityAccessManager.getRelationalWriter(
                info,
                relationship.type(),
                relationship.direction(node),
                relationType.getUnderlyingClass()
            );
        }
        if (fieldInfo == null) {
            fieldInfo = info.relationshipField(relationship.type());
        }
        if (fieldInfo != null) {
            return !fieldInfo.supportsLazyLoading();
        }
        return true;
    }

    protected void expand(StringBuilder sb, String variable, Node node, Collection<Relationship> relationships,
        int level, int depth) {
        for (Relationship relationship : relationships) {
            if (needsSeparator(sb)) {
                sb.append(", ");
            }

            listComprehension(sb, variable, relationship, node, level, depth);

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

        Collection<Relationship> relationships = getRelevantRelationships(toNode);
        if (depth > 0 && !relationships.isEmpty()) {
            sb.append(", [ ");
            expand(sb, toNodeVar, toNode, relationships, level + 1, depth - 1);
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
