/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import java.util.Collection;

import org.neo4j.ogm.compiler.SrcTargetKey;
import org.neo4j.ogm.context.Mappable;
import org.neo4j.ogm.context.TransientRelationship;

/**
 * Maintains contextual information throughout the process of compiling Cypher statements to persist a graph of objects.
 *
 * @author vince
 * @author Luanne Misquitta
 */
public interface CompileContext {

    void registerRelationship(Mappable mappable);

    boolean removeRegisteredRelationship(Mappable mappable);

    boolean visited(Object entity, int horizon);

    NodeBuilder visitedNode(Object entity);

    void register(Object entity);

    void registerTransientRelationship(SrcTargetKey key, TransientRelationship object);

    void registerNewObject(Long reference, Object relationshipEntity);

    void visitRelationshipEntity(Long relationshipIdentity);

    Collection<Object> registry();

    /**
     * Stores nodeBuilder for given entity with horizon
     * if the nodeBuilder for the entity is already present it will be overwritten (or the horizon will change)
     * the caller should ensure it doesn't happen
     */
    void visit(Object entity, NodeBuilder nodeBuilder, int horizon);

    boolean visitedRelationshipEntity(Long relationshipIdentity);

    boolean deregisterIncomingRelationships(Long identity, String type, Class endNodeType, boolean relationshipEntity);

    boolean deregisterOutgoingRelationships(Long identity, String type, Class endNodeType);

    Object getNewObject(Long id);

    Compiler getCompiler();

    Long getId(Long reference);

    void registerNewId(Long reference, Long id);

    void deregister(NodeBuilder nodeBuilder);

    Collection<Mappable> getDeletedRelationships();

    Object getVisitedObject(Long reference);

    Collection<TransientRelationship> getTransientRelationships(SrcTargetKey key);
}
