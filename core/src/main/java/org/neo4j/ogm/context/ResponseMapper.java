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

import org.neo4j.ogm.response.Response;

/**
 * Specification for an object-model mapper, which can map a model M onto arbitrary Java objects.
 *
 * @param <M> The Response model
 * @author Vince Bickers
 */

public interface ResponseMapper<M> {
    /**
     * Maps the data representation in the given response onto instances of <code>T</code>.
     *
     * @param type The {@link Class} defining the type to which each entities in the response should be mapped
     * @param response The {@link org.neo4j.ogm.response.Response} object containing the data to map onto the objects
     * @return An {@link Iterable} of type <code>T</code> containing relevant data extracted from the response
     */
    <T> Iterable<T> map(Class<T> type, Response<M> response);
}
