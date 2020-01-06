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
