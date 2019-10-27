package org.neo4j.ogm.domain.lazyloading;

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
public abstract class BaseNodeEntity extends BaseEntity {

    @Index(unique = true)
    private String nodeId;

    private String name;

    private Long created;

    @Relationship(type = "CHILD_OF", direction = Relationship.OUTGOING)
    private Node childOf;

    @Relationship(type = "HAS_TYPE", direction = Relationship.OUTGOING)
    private NodeType hasType;

    @Relationship(type = "LABELED", direction = Relationship.OUTGOING)
    private Set<Label> labeled;

    @Relationship(type = "WITH_FIELD_VALUE", direction = Relationship.OUTGOING)
    private Set<NodeData> nodeData;

    public String getNodeId() {
        return nodeId;
    }

    public BaseNodeEntity setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Node getParentNode() {
        read("childOf");
        return childOf;
    }

    public BaseNodeEntity setParentNode(Node parentNode) {
        write("childOf", parentNode);
        this.childOf = parentNode;
        return this;
    }

    public BaseNodeEntity setParentNodeBidirectional(Node newParent) {
        Node currentParent = this.getParentNode();
        if (newParent == currentParent) {
            return this;
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
        write("childOf", newParent);
        this.childOf = newParent;
        return this;
    }

    public Set<Label> getLabels() {
        read("labeled");
        return labeled;
    }

    public BaseNodeEntity setLabels(Set<Label> labels) {
        write("labeled", labels);
        this.labeled = labels;
        return this;
    }

    public NodeType getNodeType() {
        read("hasType");
        return hasType;
    }

    public BaseNodeEntity setNodeType(NodeType nodeType) {
        write("hasType", nodeType);
        this.hasType = nodeType;
        return this;
    }

    public Set<NodeData> getNodeData() {
        read("nodeData");
        return nodeData;
    }

    public BaseNodeEntity setNodeData(Set<NodeData> nodeData) {
        write("nodeData", nodeData);
        this.nodeData = nodeData;
        return this;
    }

    /**
     * @return the time in millis the Node was created at
     */
    public Long getCreated() {
        return created;
    }

    /**
     * @param created the time in millis the Node was created at
     */
    public BaseNodeEntity setCreated(Long created) {
        this.created = created;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BaseNodeEntity)) {
            return false;
        }
        BaseNodeEntity that = (BaseNodeEntity) o;
        return Objects.equals(nodeId, that.nodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " - " + getNodeId() + '[' + getId() + ']';
    }
}
