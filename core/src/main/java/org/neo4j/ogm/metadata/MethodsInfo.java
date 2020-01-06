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
