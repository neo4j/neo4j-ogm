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
            throw new IllegalArgumentException("Unknown label " + label);
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
