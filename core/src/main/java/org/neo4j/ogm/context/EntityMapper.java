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

import org.neo4j.ogm.compiler.CompileContext;

/**
 * Specification for an object-graph mapper, which can map arbitrary Java objects onto Cypher data manipulation queries.
 *
 * @author Adam George
 */
public interface EntityMapper {

    /**
     * Processes the given object and any of its composite persistent objects and produces Cypher queries to persist their state
     * in Neo4j.
     *
     * @param entity The "root" node of the object graph to persist
     * @return A {@link org.neo4j.ogm.compiler.CompileContext} object containing the statements required to persist the given object to Neo4j, along
     *         with a representation of the changes to be made by the Cypher statements never <code>null</code>
     * @throws NullPointerException if invoked with <code>null</code>
     */
    CompileContext map(Object entity);

    /**
     * Processes the given object and any of its composite persistent objects to the specified depth and produces Cypher queries
     * to persist their state in Neo4j.
     *
     * @param entity The "root" node of the object graph to persist
     * @param depth The number of objects away from the "root" to traverse when looking for objects to map
     * @return A {@link CompileContext} object containing the statements required to persist the given object to Neo4j, along
     *         with a representation of the changes to be made by the Cypher statements never <code>null</code>
     * @throws NullPointerException if invoked with <code>null</code>
     */
    CompileContext map(Object entity, int depth);

}
