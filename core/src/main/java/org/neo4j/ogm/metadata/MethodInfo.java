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

import static org.neo4j.ogm.metadata.ClassInfo.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class MethodInfo {

    private final String name;
    private final ObjectAnnotations annotations;
    private final Method method;
    /**
     * Optional field holding a delegate, from which this method was derived.
     */
    private final Field delegateHolder;

    /**
     * Creates an info object for the given method and its declared annotation.
     *
     * @param method
     * @param delegateHolder
     * @return A new method info object.
     */
    static MethodInfo of(Method method, Field delegateHolder) {
        ObjectAnnotations objectAnnotations = ObjectAnnotations.of(method.getDeclaredAnnotations());
        return new MethodInfo(method, delegateHolder, objectAnnotations);
    }

    /**
     * Constructs a new {@link MethodInfo} based on the given arguments.
     *
     * @param method The method.
     */
    private MethodInfo(Method method, Field delegateHolder, ObjectAnnotations annotations) {
        this.method = method;
        this.name = method.getName();
        this.annotations = annotations;
        this.delegateHolder = delegateHolder;
    }

    public String getName() {
        return name;
    }

    boolean hasAnnotation(Class<?> annotationClass) {
        return annotations.has(annotationClass);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodInfo)) {
            return false;
        }
        MethodInfo that = (MethodInfo) o;
        return Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method);
    }

    public Object invoke(Object target, Object... args)
        throws SecurityException, IllegalAccessException, InvocationTargetException {
        if (!method.isAccessible()) {
            method.setAccessible(true);
        }
        return method.invoke(getInstanceOrDelegate(target, delegateHolder), args);
    }
}
