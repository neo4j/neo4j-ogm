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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Node represents nodes in the schema
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
class NodeImpl implements Node {

    private final String label;
    private final Map<String, Relationship> relationships = new HashMap<>();
    private final Map<String, Set<String>> typesByRelationship = new HashMap<>();

    NodeImpl(String label) {
        this.label = label;
    }

    @Override
    public Optional<String> label() {
        return Optional.ofNullable(label);
    }

     @Override
    public Map<String, Relationship> relationships() {
        return relationships;
    }

    @Override
    public Collection<String> types(String relationshipName) {
        return typesByRelationship.getOrDefault(relationshipName, Collections.emptySet());
    }

    void addRelationship(String name, Relationship relationship) {

        if (relationships.containsKey(name)) {
            Relationship existingRelationship = relationships.get(name);

            Node existingStart = existingRelationship.start();
            Node existingEnd = existingRelationship.other(existingStart);

            Node newStart = relationship.start();
            Node newEnd = relationship.other(newStart);

            if (existingStart.equals(newStart) && existingEnd.equals(newEnd)) {
                Set<String> types = this.typesByRelationship.computeIfAbsent(name, key -> new HashSet<>());
                types.add(relationship.type());
                return;
            }
        }

        synchronized (this) {
            relationships.put(name, relationship);
            typesByRelationship.put(name, new HashSet<>(Arrays.asList(relationship.type())));
        }
    }

    @Override
    public String toString() {
        return "NodeImpl{" +
            "label='" + label +
            '}';
    }
}
