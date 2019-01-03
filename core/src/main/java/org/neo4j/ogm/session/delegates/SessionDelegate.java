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
package org.neo4j.ogm.session.delegates;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.FilterWithRelationship;
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
            FieldInfo fieldInfo = classInfo.getFieldInfo(filter.getPropertyName());
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
            } else if (filter.isDeepNested()) {
                Class parentOwnerType = filter.getOwnerEntityType();
                for (Filter.NestedPathSegment nestedPathSegment : filter.getNestedPath()) {
                    resolveRelationshipType(parentOwnerType, nestedPathSegment);
                    ClassInfo nestedClassInfo = session.metaData().classInfo(nestedPathSegment.getPropertyType().getName());
                    nestedPathSegment.setNestedEntityTypeLabel(session.metaData().entityType(nestedClassInfo.name()));
                    if (session.metaData().isRelationshipEntity(nestedClassInfo.name())) {
                        nestedPathSegment.setNestedRelationshipEntity(true);
                    }
                    parentOwnerType = nestedPathSegment.getPropertyType();
                }

            }
        }
    }

    private void resolveRelationshipType(Filter filter) {
        ClassInfo classInfo = session.metaData().classInfo(filter.getOwnerEntityType().getName());
        FieldInfo fieldInfo = classInfo.relationshipFieldByName(filter.getNestedPropertyName());

        String defaultRelationshipType = RelationshipUtils.inferRelationshipType(filter.getNestedPropertyName());
        updateRelationship(filter, fieldInfo, defaultRelationshipType);
    }

    private void resolveRelationshipType(Class parentOwnerType, Filter.NestedPathSegment segment) {
        ClassInfo classInfo = session.metaData().classInfo(parentOwnerType.getName());
        FieldInfo fieldInfo = classInfo.relationshipFieldByName(segment.getPropertyName());

        String defaultRelationshipType = RelationshipUtils.inferRelationshipType(segment.getPropertyName());
        updateRelationship(segment, fieldInfo, defaultRelationshipType);
    }

    private void updateRelationship(FilterWithRelationship filter, FieldInfo fieldInfo, String relationshipType) {
        filter.setRelationshipType(relationshipType);
        filter.setRelationshipDirection(Relationship.UNDIRECTED);
        if (fieldInfo.getAnnotations() != null) {
            AnnotationInfo annotation = fieldInfo.getAnnotations().get(Relationship.class);
            if (annotation != null) {
                filter.setRelationshipType(annotation.get(Relationship.TYPE, relationshipType));
                filter.setRelationshipDirection(annotation.get(Relationship.DIRECTION, Relationship.UNDIRECTED));
            }
            if (fieldInfo.getAnnotations().get(StartNode.class) != null) {
                filter.setRelationshipDirection(Relationship.OUTGOING);
            }
            if (fieldInfo.getAnnotations().get(EndNode.class) != null) {
                filter.setRelationshipDirection(Relationship.INCOMING);
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
