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

import java.lang.annotation.Annotation;

/**
 * A common delegate for detecting Kotlin's presence and for identifying Kotlin types.
 * This version has been adapted from Spring Framework's {@code org.springframework.core.KotlinDetector}. Thanks to
 * Juergen and Sebastien.
 *
 * @author Juergen Hoeller
 * @author Sebastien Deleuze
 * @author Michael J. Simons
 */
@SuppressWarnings("unchecked")
public final class KotlinDetector {

    private static final Class<? extends Annotation> kotlinMetadata;

    static {
        Class<?> metadata;
        ClassLoader classLoader = KotlinDetector.class.getClassLoader();
        try {
            metadata = Class.forName("kotlin.Metadata", false, classLoader);
        } catch (ClassNotFoundException ex) {
            // Kotlin API not available - no Kotlin support
            metadata = null;
        }
        kotlinMetadata = (Class<? extends Annotation>) metadata;
    }

    /**
     * Determine whether Kotlin is present in general.
     */
    public static boolean isKotlinPresent() {
        return (kotlinMetadata != null);
    }

    /**
     * Determine whether the given {@code Class} is a Kotlin type
     * (with Kotlin metadata present on it).
     */
    public static boolean isKotlinType(Class<?> clazz) {
        return (kotlinMetadata != null && clazz.getDeclaredAnnotation(kotlinMetadata) != null);
    }

    private KotlinDetector() {
    }
}
