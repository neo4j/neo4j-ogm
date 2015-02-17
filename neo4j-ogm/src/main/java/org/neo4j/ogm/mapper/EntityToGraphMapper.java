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

import org.neo4j.ogm.cypher.compiler.CypherContext;

/**
 * Specification for an object-graph mapper, which can map arbitrary Java objects onto Cypher data manipulation queries.
 */
public interface EntityToGraphMapper {

    /**
     * Processes the given object and any of its composite persistent objects and produces Cypher queries to persist their state
     * in Neo4j.
     *
     * @param entity The "root" node of the object graph to persist
     * @return A {@link CypherContext} object containing the statements required to persist the given object to Neo4j, along
     *         with a representation of the changes to be made by the Cypher statements never <code>null</code>
     * @throws NullPointerException if invoked with <code>null</code>
     */
    CypherContext map(Object entity);

    /**
     * Processes the given object and any of its composite persistent objects to the specified depth and produces Cypher queries
     * to persist their state in Neo4j.
     *
     * @param entity The "root" node of the object graph to persist
     * @param depth The number of objects away from the "root" to traverse when looking for objects to map
     * @return A {@link CypherContext} object containing the statements required to persist the given object to Neo4j, along
     *         with a representation of the changes to be made by the Cypher statements never <code>null</code>
     * @throws NullPointerException if invoked with <code>null</code>
     */
    CypherContext map(Object entity, int depth);

}
