package org.neo4j.ogm.mapper.cypher;

import java.util.List;

/**
 * Defines a simple API for building up Cypher queries programmatically.
 */
public interface CypherBuilder {

    /**
     * Creates a relationship from the specified start node to end node with the given relationship type.
     *
     * @param startNode The {@link NodeBuilder} representation of the relationship start node
     * @param relationshipType The type of relationship to create between the nodes
     * @param endNode The {@link NodeBuilder} representation of the relationship end node
     */
    void relate(NodeBuilder startNode, String relationshipType, NodeBuilder endNode);

    /**
     * Deletes a relationship from the specified start node to end node with the given relationship type.
     *
     * @param startNode The {@link NodeBuilder} representation of the relationship start node
     * @param relationshipType The type of relationship between the nodes to delete
     * @param endNode The {@link NodeBuilder} representation of the relationship end node
     */
    void unrelate(NodeBuilder startNode, String relationshipType, NodeBuilder endNode);

    /**
     * Returns a {@link NodeBuilder} that represents a new node to be created in the database.
     *
     * @return A {@link NodeBuilder} representing a new node
     */
    NodeBuilder newNode();

    /**
     * Returns a {@link NodeBuilder} that represents a node that already exists in the database and matches the given ID.
     *
     * @param existingNodeId The ID of the node in the database
     * @return A {@link NodeBuilder} representing the ndoe in the database that corresponds to the given ID
     */
    NodeBuilder existingNode(Long existingNodeId);

    /**
     * Retrieves the Cypher queries that have been built up through this {@link CypherBuilder}.
     * <p>
     * Please node that there is no requirement that implementations of {@link CypherBuilder} provide an idempotent or
     * even deterministic implementation of this method.  Therefore, it's recommended to only call this method once
     * after all query building has been completed.
     * </p>
     *
     * @return A {@link List} of Cypher queries to be executed or an empty list if there aren't any, never <code>null</code>
     */
    List<ParameterisedQuery> getStatements();

}
