package org.neo4j.ogm.compiler;

/**
 * @author vince
 */
public interface RelationshipEmitter extends CypherEmitter, Comparable<RelationshipEmitter> {

    String reference();

    RelationshipEmitter type(String type);

    void addProperty(String key, Object value);

    void relate(String startNode, String endNode);

    String getType();

    boolean hasDirection(String undirected);

    Boolean isSingleton();

    boolean isNew();

    Long getId();

    void setSingleton(Boolean b);

    RelationshipEmitter direction(String direction);

    boolean isBidirectional();

    boolean isRelationshipEntity();

    void setRelationshipEntity(boolean relationshipEntity);
}
