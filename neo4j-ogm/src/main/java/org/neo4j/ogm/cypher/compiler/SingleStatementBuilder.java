package org.neo4j.ogm.cypher.compiler;

import org.neo4j.ogm.cypher.statement.ParameterisedStatement;

import java.util.*;

/**
 * Implementation of {@link CypherCompiler} that builds a single query for the object graph.
 */
public class SingleStatementBuilder implements CypherCompiler {

    private final IdentifierManager identifiers = new IdentifierManager();

    private final List<CypherEmitter> newNodes = new ArrayList<>();
    private final List<CypherEmitter> updatedNodes = new ArrayList<>();
    private final List<CypherEmitter> newRelationships = new ArrayList<>();
    private final List<CypherEmitter> deletedRelationships = new ArrayList<>();
    private final CypherEmitter returnClause = new ReturnClauseBuilder();

    @Override
    public void relate(String startNode, String relationshipType, String endNode) {
        newRelationships.add(new NewRelationshipBuilder(relationshipType, startNode, endNode));
    }

    @Override
    public void unrelate(String startNode, String relationshipType, String endNode) {
        deletedRelationships.add(new DeletedRelationshipBuilder(relationshipType,startNode, endNode, this.identifiers.nextIdentifier()));
    }

    @Override
    public NodeBuilder newNode() {
        NodeBuilder newNode = new NewNodeBuilder(this.identifiers.nextIdentifier());
        this.newNodes.add(newNode);
        return newNode;
    }

    @Override
    public NodeBuilder existingNode(Long existingNodeId) {
        NodeBuilder node = new ExistingNodeBuilder(this.identifiers.identifier(existingNodeId));
        this.updatedNodes.add(node);
        return node;
    }

    @Override
    public List<ParameterisedStatement> getStatements() {

        StringBuilder queryBuilder = new StringBuilder();

        Set<String> varStack = new TreeSet<>();
        Set<String> newStack = new TreeSet<>();

        Map<String, Object> parameters = new HashMap<>();

        // all create statements can be done in a single clause.
        if (! this.newNodes.isEmpty() ) {
            queryBuilder.append(" CREATE ");
            for (Iterator<CypherEmitter> it = this.newNodes.iterator() ; it.hasNext() ; ) {
                NodeBuilder node = (NodeBuilder) it.next();
                if (node.emit(queryBuilder, parameters, varStack)) {
                    newStack.add(node.reference());  // for the return clause
                    if (it.hasNext()) {
                          queryBuilder.append(", ");
                    }
                }
            }
        }

        for (CypherEmitter emitter : updatedNodes) {
            emitter.emit(queryBuilder, parameters, varStack);
        }

        for (CypherEmitter emitter : newRelationships) {
            emitter.emit(queryBuilder, null, varStack);
        }

        for (CypherEmitter emitter : deletedRelationships) {
            emitter.emit(queryBuilder, null, varStack);
        }

        returnClause.emit(queryBuilder, null, newStack);

        return Collections.singletonList(new ParameterisedStatement(queryBuilder.toString(), parameters));
    }

}
