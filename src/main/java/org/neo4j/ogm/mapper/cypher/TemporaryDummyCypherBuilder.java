package org.neo4j.ogm.mapper.cypher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serves no meaningful purpose except for satisfying the compiler in the very very short term!
 */
public final class TemporaryDummyCypherBuilder implements CypherBuilder {

    StringBuilder stuffThatHappened = new StringBuilder();
    List<NodeBuilder> newNodes = new ArrayList<>();
    List<NodeBuilder> existingNodes = new ArrayList<>();

    @Override
    public void relate(NodeBuilder startNode, String relationshipType, NodeBuilder endNode) {
        // records: (startNode)-[relationshipType]->(endNode)
        stuffThatHappened.append(startNode).append("-[").append(relationshipType).append("]->").append(endNode).append("\n");
    }

    @Override
    public NodeBuilder newNode() {
        TemporaryDummyNodeBuilder newNode = new TemporaryDummyNodeBuilder();
        newNodes.add(newNode);
        return newNode;
    }

    @Override
    public NodeBuilder existingNode(Long existingNodeId) {
        TemporaryDummyNodeBuilder node = new TemporaryDummyNodeBuilder();
        node.addProperty("id", existingNodeId);
        existingNodes.add(node);
        return node;
    }

    @Override
    public List<String> getStatements() {
        for (NodeBuilder newNode : newNodes) {
            stuffThatHappened.append(" CREATE ").append(newNode);
        }
        for (NodeBuilder existingNode : existingNodes) {
            stuffThatHappened.append(" MERGE ").append(existingNode);
        }
        return Collections.singletonList(stuffThatHappened.toString());
    }

}

class TemporaryDummyNodeBuilder implements NodeBuilder {

    Map<String, Object> props = new HashMap<>();

    @Override
    public NodeBuilder addLabel(String labelName) {
        // do nothing
        return this;
    }

    @Override
    public NodeBuilder addProperty(String propertyName, Object value) {
        props.put(propertyName, value);
        return this;
    }

    @Override
    public String toString() {
        return "(" + props + ')';
    }

    @Override
    public NodeBuilder addLabels(Iterable<String> labelName) {
        for (String label : labelName) {
            addLabel(label);
        }
        return this;
    }

    @Override
    public NodeBuilder withId(Long nodeId) {
        throw new UnsupportedOperationException("atg hasn't written this method yet");
    }

}
