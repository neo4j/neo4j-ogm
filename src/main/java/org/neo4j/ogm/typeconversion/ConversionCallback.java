/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and licence terms.  Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's licence, as noted in the LICENSE file.
 */
package org.neo4j.ogm.typeconversion;

/**
 * Defines a method to be called by the underlying OGM when it needs to perform a type conversion at runtime but no explicit
 * {@link AttributeConverter} has been found.
 *
 * @author Adam George
 */
public interface ConversionCallback {

    /**
     * Convert the given value into an instance of the the specified target type.
     *
     * @param sourceType The source type to convert from
     * @param targetType The target type to convert into
     * @param value The value to be converted
     * @return The converted object, which should be an instance of the specified target type or <code>null</code> if the given
     *         value to convert is <code>null</code>
     */
    <T> T convert(Class<?> sourceType, Class<T> targetType, Object value);

}
