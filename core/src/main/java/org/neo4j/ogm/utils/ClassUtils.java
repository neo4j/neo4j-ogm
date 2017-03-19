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

package org.neo4j.ogm.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
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
        if (descriptorTypeMappings.containsKey(descriptor)) {
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
