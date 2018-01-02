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
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.metadata.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author Frantisek Hartman
 */
public class GenericUtils {

    /**
     * Tries to discover type of given field
     * If the field ha a concrete type then there is nothing to do and it's type is returned.
     * If the field has a generic type then it traverses class hierarchy of the concrete class to discover
     * ParameterizedType with type parameter
     *
     * @param field         field
     * @param concreteClass concrete class that either declares the field or is a subclass of such class
     * @return type of the field
     */
    public static Class findFieldType(Field field, Class concreteClass) {

        if (field.getGenericType() instanceof Class) {
            return field.getType();
        }

        TypeVariable[] typeParameters = field.getDeclaringClass().getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            if (typeParameters[i].getName().equals(field.getGenericType().getTypeName())) {

                ParameterizedType genericSuperclass = findMatchingSuperclass(concreteClass, field);
                if (genericSuperclass != null) {
                    return (Class) genericSuperclass.getActualTypeArguments()[i];
                }
            }
        }

        return field.getType();
    }

    /**
     * Find a generic superclass of given class that matches declaring class of given field
     *
     * @param clazz concrete class
     * @param field field
     * @return superclass as ParameterizedType
     */
    private static ParameterizedType findMatchingSuperclass(Class clazz, Field field) {
        Type superclass = clazz.getGenericSuperclass();
        if (superclass == null) {
            return null;
        }

        if (superclass instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) superclass;
            if (paramType.getRawType().equals(field.getDeclaringClass())) {
                return paramType;
            } else {
                return findMatchingSuperclass(clazz.getSuperclass(), field);
            }
        } else {
            return findMatchingSuperclass(clazz.getSuperclass(), field);
        }

    }
}
