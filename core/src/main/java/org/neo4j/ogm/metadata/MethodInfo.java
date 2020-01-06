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

package org.neo4j.ogm.metadata;

import java.lang.reflect.Method;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MethodInfo {

    private final String name;
    private final ObjectAnnotations annotations;
    private final Method method;

    /**
     * Constructs a new {@link MethodInfo} based on the given arguments.
     *
     * @param method      The method.
     * @param annotations The {@link ObjectAnnotations} applied to the field
     */
    MethodInfo(Method method, ObjectAnnotations annotations) {
        this.method = method;
        this.name = method.getName();
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    boolean hasAnnotation(String annotationName) {
        return annotations.get(annotationName) != null;
    }

    /**
     * Returns an instance of the Method represented by this MethodInfo
     * The expectation here is that only java bean getter and setter methods will be called
     *
     * @return a Method, if it exists on the corresponding class.
     */
    public Method getMethod() {
        return method;
    }
}
