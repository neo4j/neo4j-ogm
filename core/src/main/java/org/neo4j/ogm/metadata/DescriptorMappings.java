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
package org.neo4j.ogm.metadata;

import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.neo4j.ogm.config.Configuration;

/**
 * Utility class helping with descriptor to type mappings, especially providing maps of primitives and their
 * corresponding object wrapper classes ("autoboxers").
 *
 * @author Michael J. Simons
 */
public final class DescriptorMappings {

    /**
     * Contains the mapping for descriptors of primitives..
     */
    private static final Map<String, Class<?>> DESCRIPTORS_OF_PRIMITIVES = Arrays
        .asList(char.class, byte.class, short.class, int.class, long.class, float.class, double.class,
            boolean.class)
        .stream()
        .collect(collectingAndThen(toMap(Class::getName, Function.identity()), Collections::unmodifiableMap));

    /**
     * Contains the mapping for descriptors of object wrappers or "autoboxers" plus {@code java.lang.Object} and {@code java.lang.String}.
     */
    private static final Map<String, Class<?>> DESCRIPTORS_OF_WRAPPERS = Arrays
        .asList(Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class,
            Boolean.class, Object.class, String.class)
        .stream()
        .collect(collectingAndThen(toMap(Class::getName, Function.identity()), Collections::unmodifiableMap));

    /**
     * Contains the mapping of all other descriptors and is filled dynamically.
     */
    private static Map<String, Optional<Class<?>>> descriptorsToTypeMappingCache = new ConcurrentHashMap<>();

    static boolean describesPrimitve(String descriptor) {
        return DESCRIPTORS_OF_PRIMITIVES.containsKey(stripArraySuffix(descriptor));
    }

    static boolean describesWrapper(String descriptor) {
        return DESCRIPTORS_OF_WRAPPERS.containsKey(stripArraySuffix(descriptor));
    }

    /**
     * Return the reified class for the parameter of a parameterised setter or field from the parameter signature.
     * Return null if the class could not be determined
     *
     * @param descriptor parameter descriptor
     * @return reified class for the parameter or null
     */
    public static Class<?> getType(String descriptor) {

        Optional<Class<?>> optionalType = descriptorsToTypeMappingCache
            .computeIfAbsent(descriptor, k -> Optional.ofNullable(computeType(descriptor)));

        // Recompute type when it has not been computed or the cached version was loaded with a different classloader.
        boolean needsRecomputation = optionalType
            .map(type -> type.getClassLoader() != null && !isLoadedInSameHierarchy(type))
            .orElse(false);

        if (needsRecomputation) {
            optionalType = Optional.ofNullable(computeType(descriptor));
            descriptorsToTypeMappingCache.put(descriptor, optionalType);
        }
        return optionalType.orElse(null);
    }

    static boolean isLoadedInSameHierarchy(Class<?> type) {
        ClassLoader classLoader = type.getClassLoader();
        ClassLoader currentClassLoader = Configuration.getDefaultClassLoader();
        while (currentClassLoader != null) {
            if (classLoader == currentClassLoader) {
                return true;
            }
            currentClassLoader = currentClassLoader.getParent();
        }
        return false;
    }

    private static Class<?> computeType(String descriptor) {

        if (descriptor == null) {
            return null;
        }

        String rawDescriptor = stripArraySuffix(descriptor);

        if (DESCRIPTORS_OF_WRAPPERS.containsKey(rawDescriptor)) {
            return DESCRIPTORS_OF_WRAPPERS.get(rawDescriptor);
        }

        if (DESCRIPTORS_OF_PRIMITIVES.containsKey(rawDescriptor)) {
            return DESCRIPTORS_OF_PRIMITIVES.get(rawDescriptor);
        }

        if (!rawDescriptor.contains(".") && !rawDescriptor.contains("$")) {
            return Object.class;
        }

        try {
            return Class.forName(rawDescriptor, true, Configuration.getDefaultClassLoader());
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            return null;
        }
    }

    private static String stripArraySuffix(String descriptor) {
        return descriptor.replaceAll("(\\[\\])+$", "");
    }

    private DescriptorMappings() {
    }
}
