package org.neo4j.ogm.api.compiler;

import org.neo4j.ogm.api.mapper.Mappable;
import org.neo4j.ogm.api.request.Statement;

import java.util.Collection;
import java.util.List;

/**
 * @author vince
 */
public interface CompileContext {

    void setStatements(List<Statement> statements);

    void registerRelationship(Mappable mappable);

    boolean removeRegisteredRelationship(Mappable mappable);

    boolean visited(Object entity);

    NodeEmitter nodeEmitter(Object entity);

    void register(Object entity);

    void registerNewObject(String reference, Object relationshipEntity);

    void visitRelationshipEntity(Object relationshipEntity);

    Collection<Object> registry();

    void visit(Object entity, NodeEmitter nodeBuilder);

    boolean visitedRelationshipEntity(Object relationshipEntity);

    boolean deregisterIncomingRelationships(Long identity, String type, Class endNodeType, boolean relationshipEntity);

    boolean deregisterOutgoingRelationships(Long identity, String type, Class endNodeType);

    List<Statement> getStatements();

    Object getNewObject(String variable);
}
