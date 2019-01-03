/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public abstract class ClassUtils {

    private static final String primitives = "char,byte,short,int,long,float,double,boolean";

    private static Map<String, Class<?>> descriptorTypeMappings = new HashMap<>();

    /**
     * Return the reified class for the parameter of a parameterised setter or field from the parameter signature.
     * Return null if the class could not be determined
     *
     * @param descriptor parameter descriptor
     * @return reified class for the parameter or null
     */
    public static Class<?> getType(String descriptor) {
        Class<?> clazz = descriptorTypeMappings.get(descriptor);
        // check for class loader here - it can change with tools like spring-devtools
        if (clazz != null && clazz.getClassLoader() == Thread.currentThread().getContextClassLoader()) {
            return descriptorTypeMappings.get(descriptor);
        }
        Class<?> type;
        try {
            type = computeType(descriptor);
        } catch (Throwable t) {
            //return null and swallow the exception
            return null;
        }
        descriptorTypeMappings.put(descriptor, type);
        return type;
    }

    private static Class<?> computeType(String descriptor) throws ClassNotFoundException {

        if (descriptor == null) {
            return null;
        }

        if (descriptor.endsWith("[]")) {
            descriptor = descriptor.substring(0, descriptor.length() - 2);
        }

        if (descriptor.equals(String.class.getName())) {
            return String.class;
        }

        if (descriptor.equals(Character.class.getName())) {
            return Character.class;
        }

        if (descriptor.equals(Byte.class.getName())) {
            return Byte.class;
        }

        if (descriptor.equals(Short.class.getName())) {
            return Short.class;
        }

        if (descriptor.equals(Integer.class.getName())) {
            return Integer.class;
        }

        if (descriptor.equals(Long.class.getName())) {
            return Long.class;
        }

        if (descriptor.equals(Float.class.getName())) {
            return Float.class;
        }

        if (descriptor.equals(Double.class.getName())) {
            return Double.class;
        }

        if (descriptor.equals(Boolean.class.getName())) {
            return Boolean.class;
        }

        if (descriptor.equals("char")) {
            return char.class;
        }

        if (descriptor.equals("byte")) {
            return byte.class;
        }

        if (descriptor.equals("short")) {
            return short.class;
        }

        if (descriptor.equals("int")) {
            return int.class;
        }

        if (descriptor.equals("long")) {
            return long.class;
        }

        if (descriptor.equals("float")) {
            return float.class;
        }

        if (descriptor.equals("double")) {
            return double.class;
        }

        if (descriptor.equals("boolean")) {
            return boolean.class;
        }

        if (!descriptor.contains(".") && !descriptor.contains("$")) {
            return Object.class;
        }

        return Class.forName(descriptor, false, Thread.currentThread().getContextClassLoader());
    }
}
