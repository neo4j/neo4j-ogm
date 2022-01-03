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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class MethodsInfo {

    private final Set<MethodInfo> methods;

    MethodsInfo(Class<?> cls, Field delegateHolder) {
        this.methods = new HashSet<>();

        Class<?> currentClass = cls;
        do {
            Set<MethodInfo> methodInfoOfCurrentClass = Arrays.stream(currentClass.getDeclaredMethods()) //
                .filter(MethodsInfo::includeMethod) //
                .map(method -> MethodInfo.of(method, delegateHolder)) //
                .collect(toSet());

            // Prioritize annotated methods from concrete classes respectively classes lower in the hierarchy.
            methods.addAll(methodInfoOfCurrentClass);
            currentClass = currentClass.getSuperclass();
        } while (currentClass != null);
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
