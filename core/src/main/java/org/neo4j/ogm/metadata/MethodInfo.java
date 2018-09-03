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

package org.neo4j.ogm.metadata;

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
     * Creates an info object for the given method and its declared annotation.
     *
     * @param method
     * @return A new method info object.
     */
    static MethodInfo of(Method method) {
        ObjectAnnotations objectAnnotations = ObjectAnnotations.of(method.getDeclaredAnnotations());
        return new MethodInfo(method, objectAnnotations);
    }

    /**
     * Constructs a new {@link MethodInfo} based on the given arguments.
     *
     * @param method The method.
     */
    private MethodInfo(Method method, ObjectAnnotations annotations) {
        this.method = method;
        this.name = method.getName();
        this.annotations = annotations;
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
        if (this == o)
            return true;
        if (!(o instanceof MethodInfo))
            return false;
        MethodInfo that = (MethodInfo) o;
        return Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method);
    }
}
