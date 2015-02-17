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

import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.mapper.MappedRelationship;

import java.util.*;

/**
 * Maintains contextual information throughout the process of compiling Cypher statements to persist a graph of objects.
 */
public class CypherContext {

    private final Map<Object, NodeBuilder> visitedObjects = new HashMap<>();

    private final Map<String, Object> createdObjects = new HashMap<>();
    private final Collection<MappedRelationship> registeredRelationships = new HashSet<>();

    private final Collection<Object> log = new HashSet<>();

    private List<ParameterisedStatement> statements;

    public boolean visited(Object obj) {
        return this.visitedObjects.containsKey(obj);
    }

    public void visit(Object toPersist, NodeBuilder nodeBuilder) {
        this.visitedObjects.put(toPersist, nodeBuilder);
    }

    public void registerRelationship(MappedRelationship mappedRelationship) {
        this.registeredRelationships.add(mappedRelationship);
    }

    public NodeBuilder retrieveNodeBuilderForObject(Object obj) {
        return this.visitedObjects.get(obj);
    }

    public boolean isRegisteredRelationship(MappedRelationship mappedRelationship) {
        return this.registeredRelationships.contains(mappedRelationship);
    }

    public void setStatements(List<ParameterisedStatement> statements) {
        this.statements = statements;
    }

    public List<ParameterisedStatement> getStatements() {
        return this.statements;
    }

    public void registerNewObject(String cypherName, Object toPersist) {
        createdObjects.put(cypherName, toPersist);
    }

    public Object getNewObject(String cypherName) {
        return createdObjects.get(cypherName);
    }

    public Collection<MappedRelationship> registeredRelationships() {
        return registeredRelationships;
    }

    public void log(Object object) {
        log.add(object);
    }

    public Collection<Object> log() {
        return log;
    }

    public void deregisterOutgoingRelationships(Long src, String relationshipType) {
        Iterator<MappedRelationship> iterator = registeredRelationships.iterator();
        while (iterator.hasNext()) {
           MappedRelationship mappedRelationship = iterator.next();
           if (mappedRelationship.getStartNodeId() == src && mappedRelationship.getRelationshipType().equals(relationshipType)) {
               iterator.remove();
           }
        }
    }

    public void deregisterIncomingRelationships(Long tgt, String relationshipType) {
        Iterator<MappedRelationship> iterator = registeredRelationships.iterator();
        while (iterator.hasNext()) {
            MappedRelationship mappedRelationship = iterator.next();
            if (mappedRelationship.getEndNodeId() == tgt && mappedRelationship.getRelationshipType().equals(relationshipType)) {
                iterator.remove();
            }
        }
    }
}