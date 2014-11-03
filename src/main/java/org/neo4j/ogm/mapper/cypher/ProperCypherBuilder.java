package org.neo4j.ogm.mapper.cypher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A proper attempt at doing a {@link CypherBuilder}, the name of which will change once I've decided on the appropriate
 * underlying implementation.
 */
public class ProperCypherBuilder implements CypherBuilder {

    StringBuilder stuffThatHappened = new StringBuilder();
    List<NodeBuilder> newNodes = new ArrayList<>();
    List<NodeBuilder> existingNodes = new ArrayList<>();
    List<String> relationships = new ArrayList<>();

    @Override
    public void relate(NodeBuilder startNode, String relationshipType, NodeBuilder endNode) {
        // records: (startNode)-[relationshipType]->(endNode)
        stuffThatHappened.append(startNode).append("-[").append(relationshipType).append("]->").append(endNode).append("\n");
        relationships.add("MERGE (" + ((ProperNodeBuilder) startNode).variableName + ")-[:" + relationshipType + "]->("
                + ((ProperNodeBuilder) endNode).variableName + ')');
    }

    @Override
    public NodeBuilder newNode() {
        NodeBuilder newNode = new ProperNodeBuilder();
        newNodes.add(newNode);
        return newNode;
    }

    @Override
    public NodeBuilder existingNode(Long existingNodeId) {
        NodeBuilder node = new ProperNodeBuilder().withId(existingNodeId);
        existingNodes.add(node);
        return node;
    }

    @Override
    public List<String> getStatements() {
        StringBuilder sb = new StringBuilder();

        String carriedVariableNames = "";
        for (Iterator<NodeBuilder> it = this.newNodes.iterator() ; it.hasNext() ; ) {
            ProperNodeBuilder pnb = (ProperNodeBuilder) it.next();
            // MERGE is arguably safer than CREATE here in a multi-threaded environment, but is also slower
            sb.append("CREATE ");
            sb.append('(');
            sb.append(pnb.variableName);
            for (String label : pnb.labels) {
                sb.append(':').append(label);
            }
            sb.append('{');
            for (Map.Entry<String, Object> string : pnb.props.entrySet()) {
                sb.append(string.getKey()).append(':');
                sb.append("\"").append(string.getValue()).append("\",");
            }
            sb.setLength(sb.length() - 1); // delete trailing comma
            sb.append('}').append(')');
            if (it.hasNext()) {
                carriedVariableNames += pnb.variableName;
                sb.append(" WITH ").append(carriedVariableNames);
                carriedVariableNames += ", ";
            } else {
                carriedVariableNames += pnb.variableName;
            }
        }
        if (!this.existingNodes.isEmpty() && sb.length() > 0) {
            sb.append(" WITH ").append(carriedVariableNames).append(' ');
            carriedVariableNames += ", ";
        }
        for (Iterator<NodeBuilder> it = this.existingNodes.iterator() ; it.hasNext() ; ) {
            ProperNodeBuilder pnb = (ProperNodeBuilder) it.next();
            sb.append(" MATCH (").append(pnb.variableName).append(")");
            // now then, we should really have parameters returned along with the statements, shouldn't we?
            sb.append(" WHERE id(").append(pnb.variableName).append(")=").append(pnb.nodeId);
            sb.append(" SET ");
            if (!pnb.labels.isEmpty()) {
                sb.append(pnb.variableName);
                for (String label : pnb.labels) {
                    sb.append(':').append(label);
                }
                sb.append(", ");
            }
            for (Map.Entry<String, Object> string : pnb.props.entrySet()) {
                //TODO: use a DSL for value escaping if nothing else
                sb.append(pnb.variableName).append('.').append(string.getKey()).append('=');
                sb.append("\"").append(string.getValue()).append("\",");
            }
            sb.setLength(sb.length() - 1); // delete trailing comma
            sb.append(' ');
            if (it.hasNext()) {
                carriedVariableNames += pnb.variableName;
                sb.append(" WITH ").append(carriedVariableNames);
                carriedVariableNames += ", ";
            } else {
                carriedVariableNames += pnb.variableName;
            }
        }
        if (!this.relationships.isEmpty()) {
            sb.append(" WITH ").append(carriedVariableNames).append(' ');
        }
        for (String rel : this.relationships) {
            sb.append(rel).append(' ');
        }

        List<String> cypherStatements = new ArrayList<>();
        cypherStatements.add(sb.toString());
        return cypherStatements;
    }

}

class ProperNodeBuilder implements NodeBuilder {

    private static String nextVar = "a";

    final String variableName;

    ProperNodeBuilder() {
        this.variableName = nextVar;
        nextVar = Character.toString((char) (nextVar.charAt(0) + 1));
    }

    Long nodeId;
    Map<String, Object> props = new HashMap<>();
    List<String> labels = new ArrayList<>();

    @Override
    public NodeBuilder addLabel(String labelName) {
        this.labels.add(labelName);
        return this;
    }

    @Override
    public NodeBuilder addProperty(String propertyName, Object value) {
        this.props.put(propertyName, value);
        return this;
    }

    @Override
    public String toString() {
        return this.labels + "(" + this.props + ')';
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
        this.nodeId = nodeId;
        return this;
    }

}
