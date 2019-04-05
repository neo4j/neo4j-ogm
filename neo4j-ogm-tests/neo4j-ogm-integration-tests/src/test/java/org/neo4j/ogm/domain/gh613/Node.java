package org.neo4j.ogm.domain.gh613;

import java.util.HashSet;
import java.util.Objects;
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

    @Relationship(type = "BELONGS_TO")
    protected Node belongsTo;

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

    public Node getBelongsTo() {
        return belongsTo;
    }

    public Node setBelongsTo(Node belongsTo) {
        this.belongsTo = belongsTo;
        return this;
    }

    public Set<Label> getLabels() {
        return labels;
    }

    public Node setLabels(Set<Label> labels) {
        this.labels = labels;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Node)) {
            return false;
        }
        Node that = (Node) o;
        return Objects.equals(nodeId, that.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    @Override
    public String toString() {
        return "Node{" +
            "nodeId='" + nodeId + '\'' +
            '}';
    }
}
