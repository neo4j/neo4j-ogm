/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.session;

import org.neo4j.ogm.model.Property;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class Utils {

    public static final Map<String, Object> map(final Object... keysAndValues) {
        return new HashMap<String, Object>() {
            {
                for (int i = 0; i < keysAndValues.length; i+=2 ) {
                    put(String.valueOf(keysAndValues[i]), keysAndValues[i+1]);
                }
            }
        };
    }

    public static final Map<String, Object> mapCollection(final String collectionName, final Collection<Property<String, Object>> properties) {

        return new HashMap<String, Object>() {
            {
                final Map<String, Object> values = new HashMap<>();
                for (Property<String, Object> property : properties) {
                    String key = property.getKey();
                    Object value = property.asParameter();
                    if (value != null) {
                        values.put(key, value);
                    }
                }
                put(collectionName, values);
            }
        };
    }

    public static int size(Iterable<?> iterable) {
        return (iterable instanceof Collection)
                       ? ((Collection<?>) iterable).size()
                       : size(iterable.iterator());
    }

    public static int size(Iterator<?> iterator) {
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        return count;
    }

    /**
     * Coerce numeric types when mapping properties from nodes to entities.
     * This deals with numeric types - Longs to ints, Doubles to floats, Integers to bytes.
     *
     * @param clazz the entity field type
     * @param value the property value
     * @return converted value
     */
    public static Object coerceTypes(Class clazz, Object value) {
        if (clazz.isPrimitive() && value==null) {
            return defaultForPrimitive(clazz,value);
        }
        if (value != null) {
            String className = clazz.getName();
            // downcast to int from long
            if ("int".equals(className) || Integer.class.equals(clazz)) {
                if (value.getClass().equals(Long.class)) {
                    Long longValue = (Long) value;
                    if (longValue < Integer.MIN_VALUE || longValue > Integer.MAX_VALUE) {
                        throw new IllegalArgumentException(longValue + " cannot be cast to int without an overflow.");
                    }
                    return longValue.intValue();
                }
            }
            // downcast to float from double or cross-cast from int or long
            if ("float".equals(className) || (Float.class.equals(clazz))) {
                if (value.getClass().equals(Double.class)) {
                    Double dblValue = (Double) value;
                    if (dblValue < -(Float.MAX_VALUE) || dblValue > Float.MAX_VALUE) {
                        throw new IllegalArgumentException(dblValue + " cannot be cast to float without an overflow.");
                    }
                    return dblValue.floatValue();
                }
                if (value.getClass().equals(Integer.class)) {
                    Integer intValue = (Integer) value;
                    if (intValue < -(Double.MAX_VALUE) || intValue > Double.MAX_VALUE) {
                        throw new IllegalArgumentException(intValue + " cannot be cast to float without an overflow.");
                    }
                    return (float) intValue;
                }
                if (value.getClass().equals(Long.class)) {
                    Long longValue = (Long) value;
                    if (longValue < -(Double.MAX_VALUE) || longValue > Double.MAX_VALUE) {
                        throw new IllegalArgumentException(longValue + " cannot be cast to float without an overflow.");
                    }
                    return (float) longValue;
                }
            }
            // down-cast to byte from integer or long
            if ("byte".equals(className) || Byte.class.equals(clazz)) {
                if (value.getClass().equals(Integer.class)) {
                    Integer intValue = (Integer) value;
                    if (intValue < Byte.MIN_VALUE || intValue > Byte.MAX_VALUE) {
                        throw new IllegalArgumentException(intValue + " cannot be cast to byte without an overflow.");
                    }
                    return intValue.byteValue();
                }
                if (value.getClass().equals(Long.class)) {
                    Long longValue = (Long) value;
                    if (longValue < Byte.MIN_VALUE || longValue > Byte.MAX_VALUE) {
                        throw new IllegalArgumentException(longValue + " cannot be cast to byte without an overflow.");
                    }
                    return longValue.byteValue();
                }

            }
            // cross-cast to double from int or long or up-cast from float
            if ("double".equals(className) || Double.class.equals(clazz)) {
                if (value.getClass().equals(Integer.class)) {
                    Integer intValue = (Integer) value;
                    if (intValue < -(Double.MAX_VALUE) || intValue > Double.MAX_VALUE) {
                        throw new IllegalArgumentException(intValue + " cannot be cast to double without an overflow.");
                    }
                    return (double) intValue;
                }
                if (value.getClass().equals(Long.class)) {
                    Long testValue = (Long) value;
                    if (testValue < -(Double.MAX_VALUE) || testValue > Double.MAX_VALUE) {
                        throw new IllegalArgumentException(testValue + " cannot be cast to double without an overflow.");
                    }
                    return (double) testValue;
                }
                if (value.getClass().equals(Float.class)) {
                    Float floatValue = (Float) value;
                    return (double) floatValue;
                }
            }
            // up-cast to long from int
            if ("long".equals(className) || Long.class.equals(clazz)) {
                if (value.getClass().equals(Integer.class)) {
                    Integer intValue = (Integer) value;
                    return (long) intValue;
                }
            }

            // down-cast to short from int or long
            if ("short".equals(className) || Short.class.equals(clazz)) {
                if (value.getClass().equals(Long.class)) {
                    Long longValue = (Long) value;
                    if (longValue < Short.MIN_VALUE || longValue > Short.MAX_VALUE) {
                        throw new IllegalArgumentException(longValue + " cannot be cast to short without an overflow.");
                    }
                    return longValue.shortValue();
                }
                if (value.getClass().equals(Integer.class)) {
                    Integer intValue = (Integer) value;
                    if (intValue < Short.MIN_VALUE || intValue > Short.MAX_VALUE) {
                        throw new IllegalArgumentException(intValue + " cannot be cast to short without an overflow.");
                    }
                    return intValue.shortValue();
                }
            }
        }
        return value;
    }


    private static Object defaultForPrimitive(Class clazz, Object value) {
        String className = clazz.getName();
        if ("int".equals(className) || "byte".equals(className) || "short".equals(className)) {
            return 0;
        }
        if ("double".equals(className)) {
            return 0.0d;
        }
        if ("float".equals(className)) {
            return 0.0f;
        }
        if ("long".equals(className)) {
            return 0l;
        }
        return value;
    }
}
