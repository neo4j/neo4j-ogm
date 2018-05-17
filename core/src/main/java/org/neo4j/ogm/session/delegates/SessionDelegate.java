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
package org.neo4j.ogm.session.delegates;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.SortClause;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.utils.RelationshipUtils;

/**
 * @author Gerrit Meier
 */
abstract class SessionDelegate {

    final Neo4jSession session;

    SessionDelegate(Neo4jSession session) {
        this.session = session;
    }

    SortOrder sortOrderWithResolvedProperties(Class entityType, SortOrder sortOrder) {
        return SortOrder.fromSortClauses(sortClausesWithResolvedProperties(entityType, sortOrder));
    }

    void resolvePropertyAnnotations(Class entityType, Iterable<Filter> filters) {
        for (Filter filter : filters) {
            if (filter.getOwnerEntityType() == null) {
                filter.setOwnerEntityType(entityType);
            }
            String propertyName = resolvePropertyName(filter.getOwnerEntityType(), filter.getPropertyName());
            Filter.setNameFromProperty(filter, propertyName);

            ClassInfo classInfo = session.metaData().classInfo(entityType.getName());
            FieldInfo fieldInfo = classInfo.fieldsInfo().get(filter.getPropertyName());
            if (fieldInfo != null) {
                filter.setPropertyConverter(fieldInfo.getPropertyConverter());
                filter.setCompositeConverter(fieldInfo.getCompositeConverter());
            }

            if (filter.isNested()) {
                resolveRelationshipType(filter);
                ClassInfo nestedClassInfo = session.metaData().classInfo(filter.getNestedPropertyType().getName());
                filter.setNestedEntityTypeLabel(session.metaData().entityType(nestedClassInfo.name()));
                if (session.metaData().isRelationshipEntity(nestedClassInfo.name())) {
                    filter.setNestedRelationshipEntity(true);
                }
            }
        }
    }

    private void resolveRelationshipType(Filter parameter) {
        ClassInfo classInfo = session.metaData().classInfo(parameter.getOwnerEntityType().getName());
        FieldInfo fieldInfo = classInfo.relationshipFieldByName(parameter.getNestedPropertyName());

        String defaultRelationshipType = RelationshipUtils.inferRelationshipType(parameter.getNestedPropertyName());
        parameter.setRelationshipType(defaultRelationshipType);
        parameter.setRelationshipDirection(Relationship.UNDIRECTED);
        if (fieldInfo.getAnnotations() != null) {
            AnnotationInfo annotation = fieldInfo.getAnnotations().get(Relationship.class);
            if (annotation != null) {
                parameter.setRelationshipType(annotation.get(Relationship.TYPE, defaultRelationshipType));
                parameter.setRelationshipDirection(annotation.get(Relationship.DIRECTION, Relationship.UNDIRECTED));
            }
            if (fieldInfo.getAnnotations().get(StartNode.class) != null) {
                parameter.setRelationshipDirection(Relationship.OUTGOING);
            }
            if (fieldInfo.getAnnotations().get(EndNode.class) != null) {
                parameter.setRelationshipDirection(Relationship.INCOMING);
            }
        }
    }

    private List<SortClause> sortClausesWithResolvedProperties(Class entityType, SortOrder sortOrder) {

        List<SortClause> sortClausesWithResolvedProperties = new ArrayList<>();

        if (sortOrder != null) {
            for (SortClause sortClause : sortOrder.sortClauses()) {
                String[] properties = sortClause.getProperties();
                String[] resolvedProperties = new String[properties.length];
                for (int i = 0; i < properties.length; i++) {
                    resolvedProperties[i] = escapedResolvedProperty(entityType, sortClause, i);
                }
                sortClausesWithResolvedProperties.add(sortClause.fromResolvedProperties(resolvedProperties));
            }

        }
        return sortClausesWithResolvedProperties;
    }

    private String escapedResolvedProperty(Class entityType, SortClause sortClause, int i) {
        final String escapedProperty = "`%s`";

        return String
            .format(escapedProperty, resolvePropertyName(entityType, sortClause.getProperties()[i])
            );
    }

    private String resolvePropertyName(Class entityType, String propertyName) {
        ClassInfo classInfo = session.metaData().classInfo(entityType.getName());
        FieldInfo fieldInfo = classInfo.propertyFieldByName(propertyName);
        if (fieldInfo != null && fieldInfo.getAnnotations() != null) {
            AnnotationInfo annotation = fieldInfo.getAnnotations().get(Property.class);
            if (annotation != null) {
                return annotation.get(Property.NAME, propertyName);
            }
        }

        return propertyName;
    }
}
