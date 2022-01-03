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
package org.neo4j.ogm.session;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class Utils {

    /**
     * Coerce numeric types when mapping properties from nodes to entities.
     * This deals with numeric types - Longs to ints, Doubles to floats, Integers to bytes.
     *
     * @param clazz the entity field type
     * @param value the property value
     * @return converted value
     */
    public static Object coerceTypes(Class clazz, Object value) {
        if (clazz.isPrimitive() && value == null) {
            return defaultForPrimitive(clazz, null);
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
                    return intValue.floatValue();
                }
                if (value.getClass().equals(Long.class)) {
                    Long longValue = (Long) value;
                    if (longValue < -(Double.MAX_VALUE) || longValue > Double.MAX_VALUE) {
                        throw new IllegalArgumentException(longValue + " cannot be cast to float without an overflow.");
                    }
                    return longValue.floatValue();
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
                    return intValue.doubleValue();
                }
                if (value.getClass().equals(Long.class)) {
                    Long testValue = (Long) value;
                    if (testValue < -(Double.MAX_VALUE) || testValue > Double.MAX_VALUE) {
                        throw new IllegalArgumentException(
                            testValue + " cannot be cast to double without an overflow.");
                    }
                    return testValue.doubleValue();
                }
                if (value.getClass().equals(Float.class)) {
                    Float floatValue = (Float) value;
                    return floatValue.doubleValue();
                }
            }
            // up-cast to long from int
            if ("long".equals(className) || Long.class.equals(clazz)) {
                if (value.getClass().equals(Integer.class)) {
                    Integer intValue = (Integer) value;
                    return intValue.longValue();
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

            // down-cast to char from String
            if ("char".equals(className) || Character.class.equals(clazz)) {
                if (value.getClass().equals(String.class)) {
                    String stringValue = (String) value;
                    if (stringValue.length() == 1) {
                        return stringValue.charAt(0);
                    } else {
                        try {
                            return (char) Integer.parseInt(stringValue);
                        } catch (NumberFormatException nfe) {
                            throw new IllegalArgumentException(stringValue + " cannot be cast to char", nfe);
                        }
                    }
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
            return 0L;
        }
        return value;
    }
}
