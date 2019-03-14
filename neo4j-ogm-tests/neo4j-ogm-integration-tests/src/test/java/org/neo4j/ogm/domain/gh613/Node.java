/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.domain.gh613;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Andreas Berger
 */
@NodeEntity
public class Node extends BaseEntity {

    @Index(unique = true)
    private String nodeId;

    @Relationship(type = "CHILD_OF", direction = Relationship.OUTGOING)
    private Node childOf;

    @Relationship(type = "CHILD_OF", direction = Relationship.INCOMING)
    protected Set<Node> childNodes;

    @Relationship(type = "HAS_TYPE", direction = Relationship.OUTGOING)
    private NodeType nodeType;

    @Relationship(type = "LABELED", direction = Relationship.OUTGOING)
    private Set<Label> labels;

    public Node() {
    }

    public Node(String nodeId) {
        setNodeId(nodeId);
    }

    public String getNodeId() {
        return nodeId;
    }

    public Node setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public Node getChildOf() {
        return childOf;
    }

    public Node setChildOf(Node childOf) {
        this.childOf = childOf;
        return this;
    }

    public Set<Node> getChildNodes() {
        return childNodes;
    }

    public Node setChildNodes(Set<Node> childNodes) {
        this.childNodes = childNodes;
        return this;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public Node setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
        return this;
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public Node setLabels(Set<Label> labels) {
        this.labels = labels;
        return this;
    }

    public Node setChildOfBidirectional(Node newParent) {
        Node currentParent = this.getChildOf();
        if (newParent == currentParent) {
            return currentParent;
        }
        if (currentParent != null
            && currentParent.getChildNodes() != null
            && !currentParent.getChildNodes().isEmpty()) {
            // updating both sides of the bidirectional mapping
            // this is to workaround this issue https://github.com/neo4j/neo4j-ogm/issues/591
            currentParent.getChildNodes().remove(this);
        }
        if (newParent != null) {
            if (newParent.getChildNodes() == null) {
                newParent.setChildNodes(new HashSet<>());
            }
            newParent.getChildNodes().add(this);
        }
        this.childOf = newParent;
        return currentParent;
    }
}
