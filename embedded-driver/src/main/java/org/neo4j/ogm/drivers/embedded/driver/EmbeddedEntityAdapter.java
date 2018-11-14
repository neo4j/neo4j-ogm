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

package org.neo4j.ogm.drivers.embedded.driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PropertyContainer;
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
        return getAllProperties((PropertyContainer) container);
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

    public Map<String, Object> getAllProperties(PropertyContainer propertyContainer) {
        Map<String, Object> properties = new HashMap<>();
        for (String key : propertyContainer.getPropertyKeys()) {
            properties.put(key, toMapped(propertyContainer.getProperty(key)));
        }
        return properties;
    }

    public Map<String, Object> convertParameters(final Map<String, Object> originalParameter) {
        return typeSystem.getParameterConversion().convertParameters(originalParameter);
    }

    private Object toMapped(Object value) {

        if (value == null) {
            return null;
        }

        return this.typeSystem.getNativeToMappedTypeAdapter(value.getClass())
            .apply(value);
    }
}
