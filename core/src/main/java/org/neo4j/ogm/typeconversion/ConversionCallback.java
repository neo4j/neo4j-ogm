/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
package org.neo4j.ogm.typeconversion;

/**
 * Defines a method to be called by the underlying OGM when it needs to perform a type conversion at runtime but no explicit
 * {@link org.neo4j.ogm.typeconversion.AttributeConverter} has been found.
 *
 * @author Adam George
 */
public interface ConversionCallback {

    /**
     * Convert the given value into an instance of the the specified target type.
     *
     * @param <T>        the type of object returned by this conversion
     * @param targetType The target type to convert into
     * @param value      The value to be converted
     * @return The converted object, which should be an instance of the specified target type or <code>null</code> if the given
     * value to convert is <code>null</code>
     */
    <T> T convert(Class<T> targetType, Object value);
}
