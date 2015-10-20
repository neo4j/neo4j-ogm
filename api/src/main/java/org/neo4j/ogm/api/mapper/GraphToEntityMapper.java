/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.api.mapper;

import org.neo4j.ogm.api.model.Graph;

import java.util.Collection;

/**
 * Specification for an object-graph mapper, which can map {@link org.neo4j.ogm.api.model.Graph}s onto arbitrary Java objects.
 *
 * @param <G> The Graph implementation
 * @author Adam George
 */
public interface GraphToEntityMapper<G extends Graph> {

    /**
     * Maps the data representation in the given {@link org.neo4j.ogm.api.model.Graph} onto an instance of <code>T</code>.
     *
     * @param type The {@link Class} defining the type to which each entity in the graph should be mapped
     * @param graph The {@link org.neo4j.ogm.api.model.Graph} model containing the data to map onto the object
     * @return An object of type <code>T</code> containing relevant data extracted from the given graph model
     */
    <T> Collection<T> map(Class<T> type, G graph);

}
