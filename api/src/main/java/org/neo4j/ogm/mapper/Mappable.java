package org.neo4j.ogm.mapper;

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
