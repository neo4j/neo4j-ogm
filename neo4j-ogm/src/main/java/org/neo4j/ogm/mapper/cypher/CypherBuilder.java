package org.neo4j.ogm.mapper.cypher;

import java.util.List;

/**
 * Defines a simple API for building up Cypher queries programmatically.
 */
public interface CypherBuilder {

    // I'll probably make this return a RelationshipBuilder or something
    void relate(NodeBuilder startNode, String relationshipType, NodeBuilder endNode);

    NodeBuilder newNode();

    NodeBuilder existingNode(Long existingNodeId);

    /**
     * @return A {@link List} of Cypher queries to be executed
     */
    List<ParameterisedQuery> getStatements();

}
