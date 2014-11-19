package org.neo4j.ogm.session;

/**
 * Light-weight record of a relationship mapped from the database, stored as a triplet:
 * <code>startNodeId - relationshipType - endNodeId</code>
 */
public class MappedRelationship {

    private final long startNodeId;
    private final String relationshipType;
    private final long endNodeId;

    public MappedRelationship(long startNodeId, String relationshipType, long endNodeId) {
        this.startNodeId = startNodeId;
        this.relationshipType = relationshipType;
        this.endNodeId = endNodeId;
    }

    public long getStartNodeId() {
        return startNodeId;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public long getEndNodeId() {
        return endNodeId;
    }

}
