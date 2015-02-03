package org.neo4j.ogm.mapper;

/**
 * Light-weight record of a relationship mapped from the database, stored as a triplet:
 * <code>startNodeId - relationshipType - endNodeId</code>
 */
public class MappedRelationship {

    private final long startNodeId;
    private final String relationshipType;
    private final long endNodeId;

    private boolean active = true;

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

    /**
     * The default state for an existing relationship
     * is active, meaning that we don't expect to
     * delete it when the transaction commits.
     */
    public void activate() {
        active = true;
    }

    /**
     * Deactivating a relationship marks it for
     * deletion, meaning that, unless it is
     * subsequently reactivated, it will be
     * removed from the database when the
     * transaction commits.
     */
    public void deactivate() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MappedRelationship that = (MappedRelationship) o;

        if (endNodeId != that.endNodeId) return false;
        if (startNodeId != that.startNodeId) return false;
        if (!relationshipType.equals(that.relationshipType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (startNodeId ^ (startNodeId >>> 32));
        result = 31 * result + relationshipType.hashCode();
        result = 31 * result + (int) (endNodeId ^ (endNodeId >>> 32));
        return result;
    }
}
