/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.metadata.schema;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.neo4j.ogm.utils.RelationshipUtils;

/**
 * Node represents nodes in the schema
 *
 * @author Frantisek Hartman
 */
class NodeImpl implements Node {

    private final String label;
    private final Collection<String> labels;
    private final Map<String, Relationship> relationships = new HashMap<>();

    public NodeImpl(String label, Collection<String> labels) {
        this.label = label;
        this.labels = labels;
    }

    @Override
    public Optional<String> label() {
        return Optional.ofNullable(label);
    }

    @Override
    public Collection<String> labels() {
        return labels;
    }

    @Override
    public Map<String, Relationship> relationships() {
        return relationships;
    }

    void addRelationship(Relationship relationship) {
        relationships.put(RelationshipUtils.inferFieldName(relationship.type()), relationship);
    }

    void addRelationship(String name, Relationship relationship) {
        relationships.put(name, relationship);
    }

    @Override
    public String toString() {
        return "NodeImpl{" +
            "label='" + label + '\'' +
            ", labels=" + labels +
            '}';
    }
}
