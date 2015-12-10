package org.neo4j.ogm.compiler;

import java.util.Collection;

import org.neo4j.ogm.mapper.Mappable;

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

    Long newNodeId(Long reference);

    void registerNewNodeId(Long reference, Long id);

    void deregister(NodeBuilder nodeBuilder);

}
