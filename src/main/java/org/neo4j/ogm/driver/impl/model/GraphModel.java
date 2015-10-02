/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.driver.impl.model;

import org.neo4j.ogm.api.model.Edge;
import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.model.Node;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The results of a query, modelled as graph data.
 *
 *  @author Michal Bachman
 */
public class GraphModel implements Graph {

    private final Map<Long, NodeModel> nodeMap = new HashMap<>();
    private final Map<Long, RelationshipModel> relationshipMap = new HashMap<>();

    private Set<Node> nodes = new LinkedHashSet<>();
    private Set<Edge> relationships = new LinkedHashSet<>();

    public Set<Node> getNodes() {
        return nodes;
    }

    public void setNodes(NodeModel[] nodes) {
        for (NodeModel node : nodes) {
            this.nodes.add(node);
            nodeMap.put(node.getId(), node);
        }
    }

    public Set<Edge> getRelationships() {
        return relationships;
    }

    public void setRelationships(RelationshipModel[] relationships) {
        for (RelationshipModel relationship : relationships) {
            this.relationships.add(relationship);
            relationshipMap.put(relationship.getId(), relationship);
        }
    }

    public NodeModel node(Long nodeId) {
        return nodeMap.get(nodeId);
    }

    /**
     * Determines whether or not this {@link GraphModel} contains a {@link NodeModel} that matches the specified ID.
     *
     * @param nodeId The graph node ID to match against a {@link NodeModel}
     * @return <code>true</code> if this {@link GraphModel} contains a node identified by the given argument, <code>false</code>
     *         if it doesn't
     */
    public boolean containsNodeWithId(Long nodeId) {
        return nodeMap.containsKey(nodeId);
    }

    public boolean containsRelationshipWithId(Long relId) {
        return relationshipMap.containsKey(relId);
    }
}
