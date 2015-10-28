package org.neo4j.ogm.model;

/**
 * @author vince
 */
public interface Statistics {

    boolean containsUpdates();

    int getNodesCreated();

    int getNodesDeleted();

    int getPropertiesSet();

    int getRelationshipsCreated();

    int getRelationshipsDeleted();

    int getLabelsAdded();

    int getLabelsRemoved();

    int getIndexesAdded();

    int getIndexesRemoved();

    int getConstraintsAdded();

    int getConstraintsRemoved();
}
