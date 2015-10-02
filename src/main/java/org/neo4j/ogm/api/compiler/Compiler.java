/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.api.compiler;

import org.neo4j.ogm.api.request.Statement;

import java.util.List;
import java.util.Map;

/**
 * Defines a simple API for building up Cypher queries programmatically.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public interface Compiler {

    /**
     * Defines a new relationship between the specified start node to end node with the given relationship type and direction
     *
     * @deprecated Setting relationships should now be done through the {@link RelationshipEmitter}
     * @param startNode The {@link NodeEmitter} representation of the relationship start node
     * @param relationshipType The type of relationship to create between the nodes
     * @param relationshipProperties The (optional) {@code Map} containing the properties of the relationship
     * @param endNode The {@link NodeEmitter} representation of the relationship end node
     */
    @Deprecated
    void relate(String startNode, String relationshipType, Map<String, Object> relationshipProperties, String endNode);

    /**
     * Defines a relationship deletion between the specified start node to end node with the given relationship type and direction.
     *
     * @param startNode The {@link NodeEmitter} representation of the relationship start node
     * @param relationshipType The type of relationship between the nodes to delete
     * @param endNode The {@link NodeEmitter} representation of the relationship end node
     * @param relId The id of the relationship to unrelate
     */
    void unrelate(String startNode, String relationshipType, String endNode, Long relId);

    /**
     * Returns {@link NodeEmitter} that represents a new node to be created in the database.
     *
     * @return A {@link NodeEmitter} representing a new node
     */
    NodeEmitter newNode();

    /**
     * Returns a {@link NodeEmitter} that represents a node that already exists in the database and matches the given ID.
     *
     * @param existingNodeId The ID of the node in the database
     * @return A {@link NodeEmitter} representing the node in the database that corresponds to the given ID
     */
    NodeEmitter existingNode(Long existingNodeId);

    /**
     * Returns a {@link RelationshipEmitter} to use for constructing Cypher for writing a new relationship to the database.
     *
     * @return A new {@link RelationshipEmitter}
     */
    RelationshipEmitter newRelationship();

    /**
     * Returns a {@link RelationshipEmitter} to use for constructing Cypher for writing a new directed relationship in both directions to the database.
     *
     * @return A new {@link RelationshipEmitter}
     */
    RelationshipEmitter newBiDirectionalRelationship();

    /**
     * Returns a {@link RelationshipEmitter} to use for constructing Cypher to update an existing relationship in the database
     * that possesses the given ID.
     *
     * @param existingRelationshipId The ID of the relationship in the database, which shouldn't be <code>null</code>
     * @return A new {@link RelationshipEmitter} bound to the identified relationship
     */
    RelationshipEmitter existingRelationship(Long existingRelationshipId);

    /**
     * Retrieves the Cypher queries that have been built up through this {@link Compiler}.
     * <p>
     * Please node that there is no requirement that implementations of {@link Compiler} provide an idempotent or
     * even deterministic implementation of this method.  Therefore, it's recommended to only call this method once
     * after all query building has been completed.
     * </p>
     *
     * @return A {@link List} of Cypher queries to be executed or an empty list if there aren't any, never <code>null</code>
     */
    List<Statement> getStatements();

    /**
     * Returns an unused relationship's reference to the ref pool
     *
     * This is to ensure that references are only created when needed
     *
     * @param relationshipBuilder the {@link RelationshipEmitter}
     */
    void release(RelationshipEmitter relationshipBuilder);

    String nextIdentifier();

    /**
     * Returns this compiler's context
     * @return the current compiler context
     */
    CompileContext context();

    /**
     * Compiles the current request and returns the compile context, which
     * includes all the statements to be executed and related information
     * @return the current compiler context
     */
    CompileContext compile();
}
