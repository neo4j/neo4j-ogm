package org.neo4j.ogm.domain.lazyloading;

import java.util.Objects;
import java.util.Set;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Andreas Berger
 */
@NodeEntity
public class NodeType extends BaseEntity {

    @Index(unique = true)
    private String nodeTypeId;

    @JsonIgnore
    @Relationship(type = "SUBORDINATE", direction = Relationship.INCOMING)
    private Set<NodeType> subordinateNodeTypes;

    @Relationship(type = "CAN_HAVE_LABEL_OF", direction = Relationship.OUTGOING)
    private Set<LabelGroup> supportedLabelGroups;

    public NodeType() {
    }

    public NodeType(String nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
    }

    public Set<NodeType> getSubordinateNodeTypes() {
        read("subordinateNodeTypes");
        return subordinateNodeTypes;
    }

    public NodeType setSubordinateNodeTypes(Set<NodeType> subordinateNodeTypes) {
        write("subordinateNodeTypes", subordinateNodeTypes);
        this.subordinateNodeTypes = subordinateNodeTypes;
        return this;
    }

    public Set<LabelGroup> getSupportedLabelGroups() {
        read("supportedLabelGroups");
        return supportedLabelGroups;
    }

    public NodeType setSupportedLabelGroups(Set<LabelGroup> supportedLabelGroups) {
        write("supportedLabelGroups", supportedLabelGroups);
        this.supportedLabelGroups = supportedLabelGroups;
        return this;
    }

    public String getNodeTypeId() {
        return nodeTypeId;
    }

    public NodeType setNodeTypeId(String nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NodeType nodeType = (NodeType) o;
        return Objects.equals(nodeTypeId, nodeType.nodeTypeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeTypeId);
    }

    @Override
    public String toString() {
        return "NodeType: " + nodeTypeId;
    }

}
