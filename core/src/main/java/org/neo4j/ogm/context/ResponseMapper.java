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
     * @param type     The {@link Class} defining the type to which each entities in the response should be mapped
     * @param response The {@link org.neo4j.ogm.response.Response} object containing the data to map onto the objects
     * @param <T>      type of the result
     * @return An {@link Iterable} of type <code>T</code> containing relevant data extracted from the response
     */
    <T> Iterable<T> map(Class<T> type, Response<M> response);
}
