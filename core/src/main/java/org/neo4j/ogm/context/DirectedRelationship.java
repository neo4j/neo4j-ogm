/*
 * Copyright (c) 2002-2022 "Neo4j,"
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

import org.neo4j.ogm.annotation.Relationship;

/**
 * Represents a relationship type along with a direction.
 *
 * @author Luanne Misquitta
 */
public class DirectedRelationship {

    private String relationshipType;
    private Relationship.Direction relationshipDirection;

    /**
     *
     * @param relationshipType The relationship type
     * @param relationshipDirection The relationship direction
     */
    public DirectedRelationship(String relationshipType, Relationship.Direction relationshipDirection) {
        this.relationshipType = relationshipType;
        this.relationshipDirection = relationshipDirection;
    }

    public String type() {
        return relationshipType;
    }

    public Relationship.Direction direction() {
        return relationshipDirection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DirectedRelationship that = (DirectedRelationship) o;

        return relationshipType.equals(that.relationshipType) && relationshipDirection
            .equals(that.relationshipDirection);
    }

    @Override
    public int hashCode() {
        int result = relationshipType.hashCode();
        result = 31 * result + relationshipDirection.hashCode();
        return result;
    }
}
