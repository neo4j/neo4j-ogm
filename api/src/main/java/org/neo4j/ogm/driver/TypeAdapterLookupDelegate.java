/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
package org.neo4j.ogm.driver;

import static java.util.Collections.*;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * To be used by a driver to lookup type adapters, both native to mapped and mapped to native. This lookup wraps all
 * returned adapters to make resilient against null values.
 *
 * @author Michael J. Simons
 */
public final class TypeAdapterLookupDelegate {
    private final Map<Class<?>, Function> registeredTypeAdapter;

    public TypeAdapterLookupDelegate(Map<Class<?>, Function> registeredTypeAdapter) {

        this.registeredTypeAdapter = unmodifiableMap(registeredTypeAdapter);
    }

    /**
     * Retrieves an adapter for the specified class. Can be either native or mapped class.
     *
     * @param clazz The class for which an adapter is needed.
     * @return An adapter to convert an object of clazz to native or mapped, identity function if there's no adapter
     */
    public Function<Object, Object> getAdapterFor(Class<?> clazz) {

        Function<Object, Object> adapter = findAdapterFor(clazz).orElseGet(Function::identity);
        return object -> object == null ? null : adapter.apply(object);
    }

    public boolean hasAdapterFor(Class<?> clazz) {
        return findAdapterFor(clazz).isPresent();
    }

    private Optional<Function<Object, Object>> findAdapterFor(Class<?> clazz) {

        if (this.registeredTypeAdapter.containsKey(clazz)) {
            return Optional.of(registeredTypeAdapter.get(clazz));
        } else {
            return registeredTypeAdapter.entrySet()
                .stream().filter(e -> e.getKey().isAssignableFrom(clazz))
                .findFirst()
                .map(Map.Entry::getValue);
        }
    }
}
