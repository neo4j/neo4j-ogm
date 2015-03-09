/*
 * Copyright (c) 2014-2015 "GraphAware"
 *
 * GraphAware Ltd
 *
 * This file is part of Neo4j-OGM.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.neo4j.ogm.model;

import java.util.HashMap;
import java.util.Map;

public class GraphModel  {

    private final Map<Long, NodeModel> nodeMap = new HashMap<>();

    private NodeModel[] nodes = new NodeModel[]{};
    private RelationshipModel[] relationships = new RelationshipModel[]{};

    public NodeModel[] getNodes() {
        return nodes;
    }

    public void setNodes(NodeModel[] nodes) {
        this.nodes = nodes;
        for (NodeModel node : nodes) {
            nodeMap.put(node.getId(), node);
        }
    }

    public RelationshipModel[] getRelationships() {
        return relationships;
    }

    public void setRelationships(RelationshipModel[] relationships) {
        this.relationships = relationships;
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

}
