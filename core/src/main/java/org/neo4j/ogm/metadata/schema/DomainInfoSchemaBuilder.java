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
package org.neo4j.ogm.metadata.schema;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.neo4j.ogm.annotation.Relationship.Direction;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.DescriptorMappings;
import org.neo4j.ogm.metadata.DomainInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.reflect.GenericUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder of {@link Schema} which takes {@link DomainInfo} as input
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class DomainInfoSchemaBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DomainInfoSchemaBuilder.class);

    private Map<String, ClassInfo> classInfoMap;
    private SchemaImpl schema;

    public DomainInfoSchemaBuilder(DomainInfo domainInfo) {
        this.classInfoMap = domainInfo.getClassInfoMap();
        this.schema = new SchemaImpl();
    }

    public Schema build() {
        buildNodes();
        buildRelationships();
        return schema;
    }

    private void buildNodes() {
        // create nodes for all node entity classes - ClassInfo that are not relationship entities

        for (ClassInfo classInfo : classInfoMap.values()) {
            if (!classInfo.isRelationshipEntity()) {
                String label = classInfo.neo4jName();
                NodeImpl node = new NodeImpl(label);

                schema.addNode(label, node);
            }
        }
    }

    private void buildRelationships() {
        for (ClassInfo classInfo : classInfoMap.values()) {

            if (!classInfo.isRelationshipEntity()) {
                String label = classInfo.neo4jName();
                NodeImpl node = (NodeImpl) schema.findNode(label);

                collectRelationshipsInHierarchy(node, classInfo);
            } else {
                String type = classInfo.neo4jName();
                if (schema.getRelationship(type) == null) {
                    if (classInfo.getStartNodeReader() == null || classInfo.getEndNodeReader() == null) {
                        logger.warn("Start or end node not found for classInfo={}, is the metadata correct?",
                            classInfo);
                        continue;
                    }

                    NodeImpl start = getNodeByFieldAndContainingClass(classInfo, classInfo.getStartNodeReader());
                    NodeImpl end = getNodeByFieldAndContainingClass(classInfo, classInfo.getEndNodeReader());
                    schema.addRelationship(new RelationshipImpl(type, Direction.OUTGOING, start, end));
                }
            }
        }
    }

    private void collectRelationshipsInHierarchy(NodeImpl node, ClassInfo classInfo) {
        Collection<FieldInfo> relationships = classInfo.relationshipFields();

        // iterate over all relationships of all node entities
        for (FieldInfo fieldInfo : relationships) {
            createRelationship(node, fieldInfo);
        }

        for (ClassInfo subClass : classInfo.directSubclasses()) {
            collectRelationshipsInHierarchy(node, subClass);
        }
    }

    private void createRelationship(NodeImpl fromNode, FieldInfo relFieldInfo) {
        Class<?> otherClass = DescriptorMappings.getType(relFieldInfo.getTypeDescriptor());
        String otherType = otherClass.getName();
        ClassInfo otherClassInfo = classInfoMap.get(otherType);

        if (otherClassInfo == null) {
            logger.debug("Type " + otherType + " not found. Did you specify 'packages' parameter to " +
                "SessionFactory correctly?");
            return;
        }

        NodeImpl toNode;
        if (otherClassInfo.isRelationshipEntity()) {

            if (relFieldInfo.relationshipDirection() == Direction.OUTGOING) {
                toNode = getNodeByFieldAndContainingClass(otherClassInfo, otherClassInfo.getEndNodeReader());
            } else {
                // this will cover both incoming and UNDIRECTED
                // start and end type for UNDIRECTED should be same
                toNode = getNodeByFieldAndContainingClass(otherClassInfo, otherClassInfo.getStartNodeReader());
            }

        } else {
            toNode = (NodeImpl) schema.findNode(otherClassInfo.neo4jName());
        }

        RelationshipImpl relationship = new RelationshipImpl(relFieldInfo.relationshipType(),
            relFieldInfo.relationshipDirection(),
            fromNode, toNode);

        // add relationship only to fromNode, not adding to toNode because
        // - it might not declare the relationship
        // - if it does it is different direction, may have different type other side (e.g. super type of fromNode)
        // - the relationship will be created when toNode is processed
        fromNode.addRelationship(relFieldInfo.getName(), relationship);
    }

    private NodeImpl getNodeByFieldAndContainingClass(ClassInfo classInfo, FieldInfo fieldInfo) {
        NodeImpl toNode;
        Field endField = fieldInfo.getField();
        Class endType = GenericUtils.findFieldType(endField, classInfo.getUnderlyingClass());
        toNode = getNodeByTypeDescriptor(endType.getName());
        return toNode;
    }

    private NodeImpl getNodeByTypeDescriptor(String typeDescriptor) {
        ClassInfo classInfo = classInfoMap.get(typeDescriptor);
        return (NodeImpl) schema.findNode(classInfo.neo4jName());
    }
}
