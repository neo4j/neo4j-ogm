package org.graphaware.graphmodel.neo4j;

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
}
