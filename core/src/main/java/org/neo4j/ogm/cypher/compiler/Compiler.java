/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.cypher.compiler;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.StatementFactory;

/**
 * Defines a simple API for building up Cypher queries programmatically.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public interface Compiler {

    /**
     * Returns {@link NodeBuilder} that represents a new node to be created in the database.
     *
     * @return A {@link NodeBuilder} representing a new node
     */
    NodeBuilder newNode(Long id);

    /**
     * Returns a {@link RelationshipBuilder} that represents a new relationship to be created in the database
     *
     * @param type          the relationship type
     * @param bidirectional true if the relationship must be created in both incoming and outgoing directions, false otherwise
     * @return A {@link RelationshipBuilder} representing a new relationship
     */
    RelationshipBuilder newRelationship(String type, boolean bidirectional);

    /**
     * Returns a {@link RelationshipBuilder} that represents a new relationship to be created in the database
     *
     * @param type the relationship type
     * @return A {@link RelationshipBuilder} representing a new relationship
     */
    RelationshipBuilder newRelationship(String type);

    /**
     * Returns a {@link NodeBuilder} that represents a node that already exists in the database and matches the given ID.
     *
     * @param existingNodeId The ID of the node in the database
     * @return A {@link NodeBuilder} representing the node in the database that corresponds to the given ID
     */
    NodeBuilder existingNode(Long existingNodeId);

    /**
     * Returns a {@link RelationshipBuilder} that represents and existing relationship entity to be modified in the database
     *
     * @param existingRelationshipId The ID of the relationship in the database, which shouldn't be <code>null</code>
     * @param direction              The direction of the existing relationship
     * @return A new {@link RelationshipBuilder} bound to the identified relationship entity
     */
    RelationshipBuilder existingRelationship(Long existingRelationshipId, Relationship.Direction direction, String type, boolean wasDirty);

    /**
     * Defines a relationship deletion between the specified start node to end node with the given relationship type and direction.
     *  @param startNode        The reference of the relationship start node
     * @param relationshipType The type of relationship between the nodes to delete
     * @param endNode          The reference of the relationship end node
     * @param relId            The id of the relationship to unrelate
     */
    RelationshipBuilder unrelate(Long startNode, String relationshipType, Long endNode, Long relId);

    /**
     * Remove a {@link NodeBuilder}
     *
     * @param nodeBuilder The {@link NodeBuilder}
     */
    void unmap(NodeBuilder nodeBuilder);

    /**
     * Retrieves the Cypher statements that create nodes built up through this {@link Compiler}.
     *
     * @return A {@link List} of Cypher queries to be executed or an empty list if there aren't any, never <code>null</code>
     */
    List<Statement> createNodesStatements();

    /**
     * Retrieves the Cypher statements that create relationships built up through this {@link Compiler}.
     *
     * @return A {@link List} of Cypher queries to be executed or an empty list if there aren't any, never <code>null</code>
     */
    List<Statement> createRelationshipsStatements();

    /**
     * Retrieves the Cypher statements that update nodes built up through this {@link Compiler}.
     *
     * @return A {@link List} of Cypher queries to be executed or an empty list if there aren't any, never <code>null</code>
     */
    List<Statement> updateNodesStatements();

    /**
     * Retrieves the Cypher statements that update relationships built up through this {@link Compiler}.
     *
     * @return A {@link List} of Cypher queries to be executed or an empty list if there aren't any, never <code>null</code>
     */
    List<Statement> updateRelationshipStatements();

    /**
     * Retrieves the Cypher statements that delete relationships built up through this {@link Compiler}.
     *
     * @return A {@link List} of Cypher queries to be executed or an empty list if there aren't any, never <code>null</code>
     */
    List<Statement> deleteRelationshipStatements();

    /**
     * Retrieves the Cypher statements that delete relationship entities built up through this {@link Compiler}.
     *
     * @return A {@link List} of Cypher queries to be executed or an empty list if there aren't any, never <code>null</code>
     */
    List<Statement> deleteRelationshipEntityStatements();

    /**
     * Retrieves the Cypher statements that have been built up through this {@link Compiler}.
     * <p>
     * Please node that there is no requirement that implementations of {@link Compiler} provide an idempotent or
     * even deterministic implementation of this method.  Therefore, it's recommended to only call this method once
     * after all query building has been completed and to be aware that some statements may depend upon results of previous statements
     * </p>
     *
     * @return A {@link List} of Cypher queries to be executed or an empty list if there aren't any, never <code>null</code>
     */
    List<Statement> getAllStatements();

    /**
     * Returns this compiler's context
     *
     * @return the current compiler context
     */
    CompileContext context();

    /**
     * Whether there new relationships to be created that depend on nodes being created first
     *
     * @return true if there are any statements that depend on new nodes being created first
     */
    boolean hasStatementsDependentOnNewNodes();

    /**
     * Specify the {@link StatementFactory} that this {@link Compiler} will useto produce {@link Statement}s
     *
     * @param statementFactory The {@link StatementFactory}
     */
    void useStatementFactory(StatementFactory statementFactory);
}
