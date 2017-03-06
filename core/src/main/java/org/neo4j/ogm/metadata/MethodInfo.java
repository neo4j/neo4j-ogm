/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

import org.neo4j.ogm.metadata.bytecode.MetaDataClassLoader;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MethodInfo {

    private final String className;
    private final String name;
    private final ObjectAnnotations annotations;

    /**
     * Constructs a new {@link MethodInfo} based on the given arguments.
     *
     * @param name The name of the method
     * notation
     * @param annotations The {@link ObjectAnnotations} applied to the field
     */
    public MethodInfo(String className, String name, ObjectAnnotations annotations) {
        this.className = className;
        this.name = name;
        this.annotations = annotations;
    }

    public String getName() {
        return name;
    }

    public boolean hasAnnotation(String annotationName) {
        return annotations.get(annotationName) != null;
    }

    /**
     * Returns an instance of the Method represented by this MethodInfo
     * The expectation here is that only java bean getter and setter methods will be called
     *
     * @return a Method, if it exists on the corresponding class.
     */
    public Method getMethod() {
        try {
            return MetaDataClassLoader.loadClass(className).getMethod(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
