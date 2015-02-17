/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
