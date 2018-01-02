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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MethodsInfo {

    private final Map<String, MethodInfo> methods;

    MethodsInfo() {
        this.methods = new HashMap<>();
    }

    public MethodsInfo(Class<?> cls) {
        this.methods = new HashMap<>();

        for (Method method : cls.getDeclaredMethods()) {
            final int modifiers = method.getModifiers();
            if (!Modifier.isTransient(modifiers) && !Modifier.isFinal(modifiers) && !Modifier.isStatic(modifiers)) {
                ObjectAnnotations objectAnnotations = new ObjectAnnotations();
                final Annotation[] declaredAnnotations = method.getDeclaredAnnotations();
                for (Annotation annotation : declaredAnnotations) {
                    AnnotationInfo info = new AnnotationInfo(annotation);
                    objectAnnotations.put(info.getName(), info);
                }
                methods.put(method.getName(), new MethodInfo(method, objectAnnotations));
            }
        }
    }

    public MethodsInfo(Map<String, MethodInfo> methods) {
        this.methods = new HashMap<>(methods);
    }

    public Collection<MethodInfo> methods() {
        return methods.values();
    }

    public void append(MethodsInfo methodsInfo) {
        for (MethodInfo methodInfo : methodsInfo.methods()) {
            methods.putIfAbsent(methodInfo.getName(), methodInfo);
        }
    }
}
