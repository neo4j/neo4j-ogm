package org.neo4j.ogm.mapper.cypher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A proper attempt at doing a {@link CypherBuilder}, the name of which will change once I've decided on the appropriate
 * underlying implementation.
 */
public class ProperCypherBuilder implements CypherBuilder {

    List<NodeBuilder> nodes = new ArrayList<>();
    List<String> relationships = new ArrayList<>();

    @Override
    public void relate(NodeBuilder startNode, String relationshipType, NodeBuilder endNode) {
        // records: (startNode)-[relationshipType]->(endNode)
        relationships.add("MERGE (" + ((ProperNodeBuilder) startNode).variableName + ")-[:" + relationshipType + "]->("
                + ((ProperNodeBuilder) endNode).variableName + ')');
    }

    @Override
    public NodeBuilder newNode() {
        NodeBuilder newNode = new NewNodeBuilder();
        this.nodes.add(newNode);
        return newNode;
    }

    @Override
    public NodeBuilder existingNode(Long existingNodeId) {
        NodeBuilder node = new ExistingNodeBuilder().withId(existingNodeId);
        this.nodes.add(node);
        return node;
    }

    @Override
    public List<String> getStatements() {
        StringBuilder queryBuilder = new StringBuilder();
        List<String> varStack = new ArrayList<>(this.nodes.size());

        for (Iterator<NodeBuilder> it = this.nodes.iterator() ; it.hasNext() ; ) {
            ProperNodeBuilder pnb = (ProperNodeBuilder) it.next();
            pnb.renderTo(queryBuilder, varStack);
            if (it.hasNext()) {
                queryBuilder.append(" WITH ").append(toCsv(varStack));
            }
        }
        if (!this.relationships.isEmpty()) {
            queryBuilder.append(" WITH ").append(toCsv(varStack));
        }
        for (String rel : this.relationships) {
            queryBuilder.append(' ').append(rel);
        }

        return Collections.singletonList(queryBuilder.toString());
    }

    protected static String toCsv(Iterable<String> elements) {
        StringBuilder sb = new StringBuilder();
        for (String element : elements) {
            sb.append(element).append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

}

abstract class ProperNodeBuilder implements NodeBuilder {

    private static String nextVar = "a";

    protected final String variableName;
    protected final Map<String, Object> props = new HashMap<>();
    protected final List<String> labels = new ArrayList<>();
    protected Long nodeId;

    ProperNodeBuilder() {
        this.variableName = nextVar;
        // just temporary, will come up with a proper variable generation strategy soon
        nextVar = Character.toString((char) (nextVar.charAt(0) + 1));
    }

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

    protected abstract void renderTo(StringBuilder queryBuilder, List<String> varStack);

}

class ExistingNodeBuilder extends ProperNodeBuilder {

    @Override
    protected void renderTo(StringBuilder queryBuilder, List<String> varStack) {
        queryBuilder.append(" MATCH (").append(this.variableName).append(")");
        // now then, we should really have parameters returned along with the statements, shouldn't we?
        queryBuilder.append(" WHERE id(").append(this.variableName).append(")=").append(this.nodeId);
        queryBuilder.append(" SET ");
        if (!this.labels.isEmpty()) {
            queryBuilder.append(this.variableName);
            for (String label : this.labels) {
                queryBuilder.append(':').append(label);
            }
            queryBuilder.append(", ");
        }
        for (Map.Entry<String, Object> string : this.props.entrySet()) {
            //TODO: use a DSL for value escaping if nothing else
            queryBuilder.append(this.variableName).append('.').append(string.getKey()).append('=');
            queryBuilder.append("\"").append(string.getValue()).append("\",");
        }
        queryBuilder.setLength(queryBuilder.length() - 1); // delete trailing comma
        queryBuilder.append(' ');
        varStack.add(this.variableName);
    }

}

class NewNodeBuilder extends ProperNodeBuilder {

    @Override
    protected void renderTo(StringBuilder queryBuilder, List<String> varStack) {
        // MERGE is arguably safer than CREATE here in a multi-threaded environment, but is also slower
        queryBuilder.append(" CREATE ");
        queryBuilder.append('(');
        queryBuilder.append(this.variableName);
        for (String label : this.labels) {
            queryBuilder.append(':').append(label);
        }
        queryBuilder.append('{');
        for (Map.Entry<String, Object> string : this.props.entrySet()) {
            queryBuilder.append(string.getKey()).append(':');
            queryBuilder.append("\"").append(string.getValue()).append("\",");
        }
        queryBuilder.setLength(queryBuilder.length() - 1); // delete trailing comma
        queryBuilder.append('}').append(')');
        varStack.add(this.variableName);
    }

}
