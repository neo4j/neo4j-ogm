/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.context;

/**
 * Light-weight record of a relationship mapped from the database
 * <code>startNodeId - relationshipId - relationshipType - endNodeId</code>
 * The relationshipId is recorded for relationship entities, and not for simple relationships.
 * The relationship direction is always OUTGOING from the startNodeId to the endNodeId.
 * The startNodeType and endNodeType represent the class type of the entities on either end of the relationship, and may be a relationship entity class.
 *
 * @author Adam George
 * @author Luanne Misquitta
 */
public class MappedRelationship implements Mappable {

    private final long startNodeId;
    private final String relationshipType;
    private final long endNodeId;
    private Long relationshipId;
    private Class<?> startNodeType;
    private Class<?> endNodeType;

    public MappedRelationship(long startNodeId, String relationshipType, long endNodeId, Class<?> startNodeType,
        Class<?> endNodeType) {
        this.startNodeId = startNodeId;
        this.relationshipType = relationshipType;
        this.endNodeId = endNodeId;
        this.startNodeType = startNodeType;
        this.endNodeType = endNodeType;
    }

    public MappedRelationship(long startNodeId, String relationshipType, long endNodeId, Long relationshipId,
        Class<?> startNodeType, Class<?> endNodeType) {
        this.startNodeId = startNodeId;
        this.relationshipType = relationshipType;
        this.endNodeId = endNodeId;
        this.relationshipId = relationshipId;
        this.startNodeType = startNodeType;
        this.endNodeType = endNodeType;
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

    public Long getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(Long relationshipId) {
        this.relationshipId = relationshipId;
    }

    /**
     * The default state for an existing relationship
     * is active, meaning that we don't expect to
     * delete it when the transaction commits.
     */
    public void activate() {
    }

    public Class<?> getEndNodeType() {
        return endNodeType;
    }

    public Class<?> getStartNodeType() {
        return startNodeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MappedRelationship that = (MappedRelationship) o;

        return startNodeId == that.startNodeId
            && endNodeId == that.endNodeId
            && relationshipType.equals(that.relationshipType)
            && !(relationshipId != null ? !relationshipId.equals(that.relationshipId) : that.relationshipId != null);
    }

    @Override
    public int hashCode() {
        int result = (int) (startNodeId ^ (startNodeId >>> 32));
        result = 31 * result + relationshipType.hashCode();
        result = 31 * result + (int) (endNodeId ^ (endNodeId >>> 32));
        result = 31 * result + (relationshipId != null ? relationshipId.hashCode() : 0);
        return result;
    }

    public String toString() {
        return String.format("(%s)-[%s:%s]->(%s)", startNodeId, relationshipId, relationshipType, endNodeId);
    }
}
