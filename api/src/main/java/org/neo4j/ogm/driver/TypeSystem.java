/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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
     * @param clazz The class of an object that is to stored natively
     * @return True if the driver can store an object of the given class natively
     */
    default boolean supportsAsNativeType(Class<?> clazz) {
        return false;
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
     * @return The parameter conversion that fits this type system.
     */
    default ParameterConversion getParameterConversion() {
        return ObjectMapperBasedParameterConversion.INSTANCE;
    }

    /**
     * An empty set of native types.
     */
    enum NoNativeTypes implements TypeSystem {
        INSTANCE
    }
}
