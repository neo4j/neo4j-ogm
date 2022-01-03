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

import org.neo4j.ogm.cypher.compiler.CompileContext;

/**
 * Specification for an object-graph mapper, which can map arbitrary Java objects onto Cypher data manipulation queries.
 *
 * @author Adam George
 */
public interface EntityMapper {

    /**
     * Processes the given object and any of its composite persistent objects and produces Cypher queries to persist their state
     * in Neo4j.
     * NOTE: multiple map calls from same EntityMapper instance return same CompileContext with accumulated results.
     * You can also use {@link #compileContext()} to get final CompileContext.
     *
     * @param entity The "root" node of the object graph to persist
     * @return A {@link org.neo4j.ogm.cypher.compiler.CompileContext} object containing the statements required to persist the given object to Neo4j, along
     * with a representation of the changes to be made by the Cypher statements never <code>null</code>
     * @throws NullPointerException if invoked with <code>null</code>
     */
    CompileContext map(Object entity);

    /**
     * Processes the given object and any of its composite persistent objects to the specified depth and produces Cypher queries
     * to persist their state in Neo4j.
     * NOTE: multiple map calls from same EntityMapper instance return same CompileContext with accumulated results.
     * You can also use {@link #compileContext()} to get final CompileContext.
     *
     * @param entity The "root" node of the object graph to persist
     * @param depth  The number of objects away from the "root" to traverse when looking for objects to map
     * @return A {@link CompileContext} object containing the statements required to persist the given object to Neo4j, along
     * with a representation of the changes to be made by the Cypher statements never <code>null</code>
     * @throws NullPointerException if invoked with <code>null</code>
     */
    CompileContext map(Object entity, int depth);

    /**
     * Returns compile context after multiple {@link #map(Object)} operations were called
     *
     * @return CompileContext
     */
    CompileContext compileContext();
}
