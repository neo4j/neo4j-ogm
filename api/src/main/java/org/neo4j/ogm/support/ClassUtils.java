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
package org.neo4j.ogm.support;

import java.lang.reflect.Type;

/**
 * @author Michael J. Simons
 */
public final class ClassUtils {

    /**
     * See https://github.com/neo4j/neo4j-ogm/issues/643. An enum instance that overrides methods of the enum itself
     * is realized as an anonymous inner class for which {@link Class#isEnum()} returns false.
     *
     * @param clazz The class to check whether it is an enum or not.
     * @return True, if {@code clazz} is an enum.
     */
    public static boolean isEnum(Class<?> clazz) {

        return clazz.isEnum() || Enum.class.isAssignableFrom(clazz);
    }

    /**
     * @param type The type to check whether it is an enum or not.
     * @return True, if the type refers to an enum instance.
     * @see #isEnum(Class)
     */
    public static boolean isEnum(Type type) {

        return type instanceof Class<?> && isEnum((Class<?>) type);
    }

    /**
     * @param object
     * @return True, if the object is an enum instance.
     * @see #isEnum(Class)
     */
    public static boolean isEnum(Object object) {

        if (object == null) {
            return false;
        }

        return isEnum(object.getClass());
    }

    private ClassUtils() {
    }
}
