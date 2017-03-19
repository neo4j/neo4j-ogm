/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.context;

/**
 * Represents a relationship type along with a direction.
 *
 * @author Luanne Misquitta
 */
public class DirectedRelationship {

    private String relationshipType;
    private String relationshipDirection;

    public DirectedRelationship(String relationshipType, String relationshipDirection) {
        this.relationshipType = relationshipType;
        this.relationshipDirection = relationshipDirection;
    }

    public String type() {
        return relationshipType;
    }

    public String direction() {
        return relationshipDirection;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DirectedRelationship that = (DirectedRelationship) o;

        return relationshipType.equals(that.relationshipType) && relationshipDirection.equals(that.relationshipDirection);
    }

    @Override
    public int hashCode() {
        int result = relationshipType.hashCode();
        result = 31 * result + relationshipDirection.hashCode();
        return result;
    }
}
