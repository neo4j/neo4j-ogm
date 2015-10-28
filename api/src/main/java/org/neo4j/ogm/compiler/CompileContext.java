package org.neo4j.ogm.compiler;

import org.neo4j.ogm.mapper.Mappable;
import org.neo4j.ogm.request.Statement;

import java.util.Collection;
import java.util.List;

/**
 * @author vince
 */
public interface CompileContext {

    void setStatements(List<Statement> statements);

    void registerRelationship(Mappable mappable);

    boolean removeRegisteredRelationship(Mappable mappable);

    boolean visited(Long identity);

    NodeEmitter nodeEmitter(Long identity);

    void register(Object entity);

    void registerNewObject(String reference, Object relationshipEntity);

    void visitRelationshipEntity(Long relationshipIdentity);

    Collection<Object> registry();

    void visit(Long identity, NodeEmitter nodeBuilder);

    boolean visitedRelationshipEntity(Long relationshipIdentity);

    boolean deregisterIncomingRelationships(Long identity, String type, Class endNodeType, boolean relationshipEntity);

    boolean deregisterOutgoingRelationships(Long identity, String type, Class endNodeType);

    List<Statement> getStatements();

    Object getNewObject(String variable);
}
