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

import static java.util.stream.Collectors.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class MethodsInfo {

    private final Set<MethodInfo> methods;

    MethodsInfo() {
        this.methods = new HashSet<>();
    }

    public MethodsInfo(Class<?> cls) {
        this.methods = new HashSet<>();

        Class<?> currentClass = cls;
        do {
            Set<MethodInfo> methodInfoOfCurrentClass = Arrays.stream(currentClass.getDeclaredMethods()) //
                .filter(MethodsInfo::includeMethod) //
                .map(MethodInfo::of) //
                .collect(toSet());

            // Prioritize annotated methods from concrete classes respectively classes lower in the hierarchy.
            methods.addAll(methodInfoOfCurrentClass);
        } while ((currentClass = currentClass.getSuperclass()) != null);
    }

    /**
     * @param methods
     * @deprecated since 3.1.3, will be removed in 3.1.4 to reduce the public OGM surface
     */
    @Deprecated
    public MethodsInfo(Map<String, MethodInfo> methods) {
        this.methods = new HashSet<>(methods.values());
    }

    /**
     * @deprecated since 3.1.3, will be removed in 3.1.4. Use {@link #findMethodInfoBy(Predicate)} if you need access to
     * this internal API.
     */
    @Deprecated
    public Collection<MethodInfo> methods() {
        return Collections.unmodifiableCollection(methods);
    }

    Collection<MethodInfo> findMethodInfoBy(Predicate<MethodInfo> predicate) {
        return this.methods.stream().filter(predicate).collect(toList());
    }

    public void append(MethodsInfo methodsInfo) {
        this.methods.addAll(methodsInfo.methods);
    }

    private static boolean includeMethod(Method method) {

        final int modifiers = method.getModifiers();
        return !(Modifier.isTransient(modifiers) || Modifier.isStatic(modifiers));
    }
}
