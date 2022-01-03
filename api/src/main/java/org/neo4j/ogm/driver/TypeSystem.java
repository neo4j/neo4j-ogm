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
package org.neo4j.ogm.driver;

import java.util.function.Function;

/**
 * This interface describes a type system with a set of native type a driver can handle.
 *
 * @author Michael J. Simons
 * @since 3.2
 */
public interface TypeSystem {

    /**
     * True if values of the given class are supported natively.
     * See <a href="https://neo4j.com/docs/driver-manual/1.7/cypher-values/#driver-neo4j-type-system">the Cypher type system</a>
     * for reference.
     *
     * @param clazz The class of an object that is to stored natively
     * @return True if the driver can store an object of the given class natively
     * @see AbstractConfigurableDriver#DEFAULT_SUPPORTED_TYPES
     */
    default boolean supportsAsNativeType(Class<?> clazz) {

        return clazz == null ? false : AbstractConfigurableDriver.DEFAULT_SUPPORTED_TYPES.stream()
            .filter(st -> st.isAssignableFrom(clazz)).findAny().isPresent();
    }

    /**
     * @param clazz
     * @return An adapter function that can map native objects of the given class to a mapped type
     */
    default Function<Object, Object> getNativeToMappedTypeAdapter(Class<?> clazz) {
        return Function.identity();
    }

    /**
     * @param clazz
     * @return An adapter function that can mapped objects of the given class to a native type
     */
    default Function<Object, Object> getMappedToNativeTypeAdapter(Class<?> clazz) {
        return Function.identity();
    }

    /**
     * An empty set of native types.
     */
    enum NoNativeTypes implements TypeSystem {
        INSTANCE
    }
}
