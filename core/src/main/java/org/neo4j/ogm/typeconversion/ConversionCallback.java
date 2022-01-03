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
