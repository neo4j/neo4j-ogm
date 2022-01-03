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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
class SchemaImpl implements Schema {

    private Map<String, NodeImpl> nodes = new HashMap<>();
    private Map<String, Relationship> relationships = new HashMap<>();

    void addNode(String label, NodeImpl node) {
        nodes.put(label, node);
    }

    void addRelationship(RelationshipImpl relationship) {
        relationships.put(relationship.type(), relationship);
    }

    @Override
    public Node findNode(String label) {

        if (!nodes.containsKey(label)) {
            throw new IllegalArgumentException("Unknown label `" + label + "`");
        }
        return nodes.get(label);
    }

    @Override
    public Relationship findRelationship(String type) {

        if (!relationships.containsKey(type)) {
            throw new IllegalArgumentException("Unknown type " + type);
        }

        return relationships.get(type);
    }

    Relationship getRelationship(String type) {
        return relationships.get(type);
    }

}
