package org.neo4j.ogm.api.mapper;

/**
 * @author vince
 */
public interface Mappable {

    long getEndNodeId();

    long getStartNodeId();

    String getRelationshipType();

    Class getEndNodeType();

    Class getStartNodeType();
}
