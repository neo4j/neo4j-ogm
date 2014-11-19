package org.neo4j.ogm.mapper.cypher.single;

import org.neo4j.ogm.mapper.cypher.CypherBuilder;
import org.neo4j.ogm.mapper.cypher.NodeBuilder;
import org.neo4j.ogm.mapper.cypher.ParameterisedStatement;

import java.util.*;

/**
 * Implementation of {@link CypherBuilder} that builds a single query for the object graph.
 */
public class SingleQueryCypherBuilder implements CypherBuilder {

    private final IdentifierManager identifiers = new IdentifierManager();
    private final List<NodeBuilder> nodes = new ArrayList<>();
    private final List<String> relationshipsToAdd = new ArrayList<>();
    private final List<String> relationshipsToRemove = new ArrayList<>();

    @Override
    public void relate(NodeBuilder startNode, String relationshipType, NodeBuilder endNode) {
        // records: (startNode)-[relationshipType]->(endNode)
        this.relationshipsToAdd.add("MERGE (" + ((SingleQueryNodeBuilder) startNode).variableName + ")-[:" + relationshipType + "]->("
                + ((SingleQueryNodeBuilder) endNode).variableName + ')');
    }

    @Override
    public void unrelate(NodeBuilder startNode, String relationshipType, NodeBuilder endNode) {
        String relationshipIdentifier = this.identifiers.nextIdentifier();
        this.relationshipsToRemove.add("MATCH (" + ((SingleQueryNodeBuilder) startNode).variableName
                + ")-[" + relationshipIdentifier + ':' + relationshipType + "]->("
                + ((SingleQueryNodeBuilder) endNode).variableName + ") DELETE " + relationshipIdentifier);
    }

    @Override
    public NodeBuilder newNode() {
        NodeBuilder newNode = new NewNodeBuilder(this.identifiers.nextIdentifier());
        this.nodes.add(newNode);
        return newNode;
    }

    @Override
    public NodeBuilder existingNode(Long existingNodeId) {
        NodeBuilder node = new ExistingNodeBuilder(this.identifiers.nextIdentifier()).withId(existingNodeId);
        this.nodes.add(node);
        return node;
    }

    @Override
    public List<ParameterisedStatement> getStatements() {
        StringBuilder queryBuilder = new StringBuilder();
        List<String> varStack = new ArrayList<>(this.nodes.size());
        Map<String, Object> parameters = new HashMap<>();

        for (Iterator<NodeBuilder> it = this.nodes.iterator() ; it.hasNext() ; ) {
            SingleQueryNodeBuilder node = (SingleQueryNodeBuilder) it.next();
            node.renderTo(queryBuilder, parameters, varStack);
            if (it.hasNext()) {
                queryBuilder.append(" WITH ").append(toCsv(varStack));
            }
        }
        if (!this.relationshipsToAdd.isEmpty()) {
            queryBuilder.append(" WITH ").append(toCsv(varStack));
        }
        for (String rel : this.relationshipsToAdd) {
            queryBuilder.append(' ').append(rel);
        }
        if (!this.relationshipsToRemove.isEmpty()) {
            queryBuilder.append(" WITH ").append(toCsv(varStack));
        }
        for (Iterator<String> it = this.relationshipsToRemove.iterator() ; it.hasNext() ; ) {
            queryBuilder.append(' ').append(it.next());
            if (it.hasNext()) {
                queryBuilder.append(" WITH ").append(toCsv(varStack));
            }
        }

        return Collections.singletonList(new ParameterisedStatement(queryBuilder.toString(), parameters));
    }

    private static String toCsv(Iterable<String> elements) {
        StringBuilder sb = new StringBuilder();
        for (String element : elements) {
            sb.append(element).append(',');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

}
