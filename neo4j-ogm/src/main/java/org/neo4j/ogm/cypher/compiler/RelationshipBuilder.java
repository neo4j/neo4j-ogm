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

package org.neo4j.ogm.cypher.compiler;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to compile Cypher that holds information about a relationship
 */
public abstract class RelationshipBuilder implements CypherEmitter, Comparable<RelationshipBuilder> {

    protected String type;
    protected String startNodeIdentifier;
    protected String endNodeIdentifier;
    protected String reference;

    private String direction;
    final Map<String, Object> props = new HashMap<>();


    protected RelationshipBuilder(String variableName) {
        this.reference = variableName;
    }

    public String getType() {
        return this.type;
    }

    public RelationshipBuilder type(String type) {
        this.type = type;
        return this;
    }

    public void addProperty(String propertyName, Object propertyValue) {
        this.props.put(propertyName, propertyValue);
    }

    public RelationshipBuilder direction(String direction) {
        this.direction = direction;
        return this;
    }

    public boolean hasDirection(String direction) {
        return this.direction != null && this.direction.equals(direction);
    }

    public abstract void relate(String startNodeIdentifier, String endNodeIdentifier);

    public String getReference() {
        return reference;
    }

    @Override
    public int compareTo(RelationshipBuilder o) {
        return reference.compareTo(o.reference);
    }

    @Override
    public String toString() {
        return "(" + startNodeIdentifier + ")-[" + reference + ":" + type + "]->(" + endNodeIdentifier + ")";
    }
}
