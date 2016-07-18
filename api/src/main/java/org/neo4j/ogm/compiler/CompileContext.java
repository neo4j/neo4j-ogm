/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.compiler;

import org.neo4j.ogm.context.Mappable;
import java.util.Collection;

/**
 * Maintains contextual information throughout the process of compiling Cypher statements to persist a graph of objects.
 *
 * @author vince
 * @author Luanne Misquitta
 */
public interface CompileContext {

    void registerRelationship(Mappable mappable);

    boolean removeRegisteredRelationship(Mappable mappable);

    boolean visited(Long identity);

    NodeBuilder visitedNode(Long identity);

    void register(Object entity);

    void registerNewObject(Long reference, Object relationshipEntity);

    void visitRelationshipEntity(Long relationshipIdentity);

    Collection<Object> registry();

    void visit(Long identity, NodeBuilder nodeBuilder);

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

}
