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
package org.neo4j.ogm.drivers.bolt.response;

import static org.neo4j.ogm.drivers.bolt.driver.BoltDriver.NATIVE_TYPES;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Entity;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;

/**
 * Helper methods for Bolt entities
 *
 * @author Luanne Misquitta
 */
class BoltEntityAdapter {

    boolean isPath(Object value) {
        return value instanceof Path;
    }

    boolean isNode(Object value) {
        return value instanceof Node;
    }

    boolean isRelationship(Object value) {
        return value instanceof Relationship;
    }

    public long nodeId(Object node) {
        return ((Node) node).id();
    }

    public List<String> labels(Object value) {
        Node node = (Node) value;
        List<String> labels = new ArrayList<>();
        for (String label : node.labels()) {
            labels.add(label);
        }
        return labels;
    }

    public long relationshipId(Object relationship) {
        return ((Relationship) relationship).id();
    }

    public String relationshipType(Object relationship) {
        return ((Relationship) relationship).type();
    }

    public Long startNodeId(Object relationship) {
        return ((Relationship) relationship).startNodeId();
    }

    public Long endNodeId(Object relationship) {
        return ((Relationship) relationship).endNodeId();
    }

    public Map<String, Object> properties(Object container) {
        return ((Entity) container).asMap(BoltEntityAdapter::toMapped);
    }

    List<Object> nodesInPath(Object pathValue) {
        Path path = (Path) pathValue;
        List<Object> nodes = new ArrayList<>(path.length());
        for (Node node : path.nodes()) {
            nodes.add(node);
        }
        return nodes;
    }

    List<Object> relsInPath(Object pathValue) {
        Path path = (Path) pathValue;
        List<Object> rels = new ArrayList<>(path.length());
        for (Relationship rel : path.relationships()) {
            rels.add(rel);
        }
        return rels;
    }

    private static Object toMapped(Value value) {

        if (value == null) {
            return null;
        }

        Object object = value.asObject();
        return NATIVE_TYPES.getNativeToMappedTypeAdapter(object.getClass())
            .apply(object);
    }
}
