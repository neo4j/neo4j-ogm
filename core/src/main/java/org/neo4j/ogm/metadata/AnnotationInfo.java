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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.ogm.annotation.ValueFor;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 * @author Gerrit Meier
 */
public class AnnotationInfo {

    private static String convert(Method element, Object value) {

        final Class<?> returnType = element.getReturnType();
        if (returnType.isPrimitive()) {
            return String.valueOf(value);
        } else if (returnType.equals(Class.class)) {
            return ((Class) value).getName();
        } else {
            final String result = value.toString();
            if (result.isEmpty()) {
                if (element.getDefaultValue().toString().isEmpty()) {
                    return null;
                }
                return element.getDefaultValue().toString();
            }
            return result;
        }
    }

    private String annotationName;
    private Annotation annotation;

    private Map<String, String> elements;

    public AnnotationInfo(Annotation annotation) {

        this.annotationName = annotation.annotationType().getName();
        this.annotation = annotation;
        this.elements = new HashMap<>();

        final Method[] declaredElements = annotation.annotationType().getDeclaredMethods();
        for (Method element : declaredElements) {
            Object value;
            value = getAttributeValue(annotation, element);
            elements
                .put(element.getName(), value != null ? convert(element, value) : element.getDefaultValue().toString());
        }

        for (Method element : declaredElements) {
            ValueFor valueFor = element.getAnnotation(ValueFor.class);
            if (valueFor != null) {
                Object value = getAttributeValue(annotation, element);

                if (value != null && (!(value instanceof String) || StringUtils.isNotBlank((String) value))) {
                    elements.put(valueFor.value(), convert(element, value));
                }
            }
        }
    }

    private Object getAttributeValue(Annotation annotation, Method element) {
        Object value;
        try {
            value = element.invoke(annotation);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not read value of Annotation " + element.getName(), e);
        }
        return value;
    }

    public String getName() {
        return annotationName;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public String get(String key, String defaultValue) {
        elements.putIfAbsent(key, defaultValue);
        return get(key);
    }

    public String get(String key) {
        return elements.get(key);
    }
}
