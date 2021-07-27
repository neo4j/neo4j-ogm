/*
 * Copyright (c) 2002-2021 "Neo4j,"
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
package org.neo4j.ogm.drivers.embedded.driver;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Entity;
import org.neo4j.graphdb.Relationship;
import org.neo4j.ogm.driver.TypeSystem;

/**
 * Helper methods for embedded graph entities
 *
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class EmbeddedEntityAdapter {

    private final TypeSystem typeSystem;

    EmbeddedEntityAdapter(TypeSystem typeSystem) {
        this.typeSystem = typeSystem;
    }

    public boolean isPath(Object value) {
        return value instanceof Path;
    }

    public boolean isNode(Object value) {
        return value instanceof Node;
    }

    public boolean isRelationship(Object value) {
        return value instanceof Relationship;
    }

    public long nodeId(Object node) {
        return ((Node) node).getId();
    }

    public List<String> labels(Object node) {
        List<String> labels = new ArrayList<>();
        for (Label label : ((Node) node).getLabels()) {
            labels.add(label.name());
        }
        return labels;
    }

    public long relationshipId(Object relationship) {
        return ((Relationship) relationship).getId();
    }

    public String relationshipType(Object relationship) {
        return ((Relationship) relationship).getType().name();
    }

    public Long startNodeId(Object relationship) {
        return ((Relationship) relationship).getStartNode().getId();
    }

    public Long endNodeId(Object relationship) {
        return ((Relationship) relationship).getEndNode().getId();
    }

    public Map<String, Object> properties(Object container) {
        return getAllProperties((Entity) container);
    }

    public List<Object> nodesInPath(Object path) {
        List<Object> nodes = new ArrayList<>();
        for (Node node : ((Path) path).nodes()) {
            nodes.add(node);
        }
        return nodes;
    }

    public List<Object> relsInPath(Object path) {
        List<Object> rels = new ArrayList<>();
        for (Relationship rel : ((Path) path).relationships()) {
            rels.add(rel);
        }
        return rels;
    }

    public Map<String, Object> getAllProperties(Entity propertyContainer) {
        Map<String, Object> properties = new HashMap<>();
        for (String key : propertyContainer.getPropertyKeys()) {
            properties.put(key, toMapped(propertyContainer.getProperty(key)));
        }
        return properties;
    }

    private Object toMapped(Object value) {

        if (value == null) {
            return null;
        }

        if (value.getClass().isArray()) {
            return arrayToMapped(value);
        } else {
            return this.typeSystem.getNativeToMappedTypeAdapter(value.getClass()).apply(value);
        }
    }

    private Object arrayToMapped(Object array) {

        Function<Object, Object> nativeToMappedTypeAdapter = this.typeSystem
            .getNativeToMappedTypeAdapter(array.getClass().getComponentType());

        int length = Array.getLength(array);
        Object[] newArray = new Object[length];
        for (int i = 0; i < length; ++i) {
            Object object = Array.get(array, i);
            newArray[i] = nativeToMappedTypeAdapter.apply(object);
        }
        return newArray;
    }
}
