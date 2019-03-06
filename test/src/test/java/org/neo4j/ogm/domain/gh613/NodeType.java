package org.neo4j.ogm.domain.gh613;

import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;

/**
 * @author Andreas Berger
 */
@NodeEntity
public class NodeType extends BaseEntity {

    @Index(unique = true)
    private String nodeTypeId;

    public NodeType() {
    }

    public NodeType(String nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
    }

    public String getNodeTypeId() {
        return nodeTypeId;
    }

    public NodeType setNodeTypeId(String nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
        return this;
    }

    @Override
    public String toString() {
        return "NodeType: " + nodeTypeId;
    }

}
