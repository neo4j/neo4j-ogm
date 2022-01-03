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
package org.neo4j.ogm.metadata.reflect;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.metadata.KotlinDetector;

/**
 * Contains a stripped down version of <a href="https://github.com/jhalterman">Jonathan Halterman's</a>
 * <a href="https://github.com/jhalterman/typetools/blob/master/src/main/java/net/jodah/typetools/TypeResolver.java">TypeResolver</a>
 * from <a href="https://github.com/jhalterman/typetools">Typetools</a>.
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public final class GenericUtils {

    /**
     * Helper to check whether a given field is a generic field (A field described by a type variable).
     *
     * @param field The field to check for a type variable
     * @return True, if {@code field} is a generic field.
     */
    public static boolean isGenericField(Field field) {
        return field.getGenericType() instanceof TypeVariable;
    }

    /**
     * Helper to check whether a given field is a parameterized field (A field described by a type variable presenting a parameter type).
     *
     * @param field The field to check for a parameterized type
     * @return True, if {@code field} is a parameterized field.
     */
    public static boolean isParameterizedField(Field field) {
        return field.getGenericType() instanceof ParameterizedType;
    }

    /**
     * Tries to discover type of given field.
     * If the field ha a concrete type then there is nothing to do and it's type is returned.
     * If the field has a generic type then it traverses class hierarchy of the concrete class to discover
     * ParameterizedType with type parameter.
     * If the field is parameterized but no parameter can be discovered, than {@code Object.class} will be returned.
     *
     * @param field         field
     * @param concreteClass concrete class that either declares the field or is a subclass of such class
     * @return type of the field
     */
    public static Class findFieldType(Field field, Class concreteClass) {

        Class<?>[] arguments = resolveRawArguments(field.getGenericType(), concreteClass);
        if (arguments == null || arguments.length == 0 || arguments[0] == Unknown.class) {
            return isParameterizedField(field) ? Object.class : field.getType();
        }

        return arguments[0];
    }

    /**
     * Returns an array of raw classes representing arguments for the {@code genericType} using type variable information
     * from the {@code subType}. Arguments for {@code genericType} that cannot be resolved are returned as
     * {@code Unknown.class}. If no arguments can be resolved then {@code null} is returned.
     *
     * @param genericType to resolve arguments for
     * @param subType     to extract type variable information from
     * @return array of raw classes representing arguments for the {@code genericType} else {@code null} if no type
     * arguments are declared
     */
    private static Class<?>[] resolveRawArguments(Type genericType, Class<?> subType) {
        Class<?>[] result = null;
        Class<?> functionalInterface = null;

        // Handle lambdas
        if (subType.isSynthetic()) {
            Class<?> fi = genericType instanceof ParameterizedType
                && ((ParameterizedType) genericType).getRawType() instanceof Class
                ? (Class<?>) ((ParameterizedType) genericType).getRawType()
                : genericType instanceof Class ? (Class<?>) genericType : null;
            if (fi != null && fi.isInterface()) {
                functionalInterface = fi;
            }
        }

        if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type[] arguments = paramType.getActualTypeArguments();
            result = new Class[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                result[i] = resolveRawClass(arguments[i], subType, functionalInterface);
            }
        } else if (genericType instanceof TypeVariable) {
            result = new Class[1];
            result[0] = resolveRawClass(genericType, subType, functionalInterface);
        } else if (genericType instanceof Class) {
            TypeVariable<?>[] typeParams = ((Class<?>) genericType).getTypeParameters();
            result = new Class[typeParams.length];
            for (int i = 0; i < typeParams.length; i++) {
                result[i] = resolveRawClass(typeParams[i], subType, functionalInterface);
            }
        }

        return result;
    }

    private static Class<?> resolveRawClass(Type genericType, Class<?> subType, Class<?> functionalInterface) {
        if (genericType instanceof Class) {
            return (Class<?>) genericType;
        } else if (genericType instanceof ParameterizedType) {
            return resolveRawClass(((ParameterizedType) genericType).getRawType(), subType, functionalInterface);
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType arrayType = (GenericArrayType) genericType;
            Class<?> component = resolveRawClass(arrayType.getGenericComponentType(), subType, functionalInterface);
            return Array.newInstance(component, 0).getClass();
        } else if (genericType instanceof TypeVariable) {
            TypeVariable<?> variable = (TypeVariable<?>) genericType;
            genericType = getTypeVariableMap(subType, functionalInterface).get(variable);
            genericType = genericType == null ? resolveBound(variable)
                : resolveRawClass(genericType, subType, functionalInterface);
        } else if (genericType instanceof WildcardType && KotlinDetector.isKotlinType(subType)) {
            WildcardType variable = (WildcardType) genericType;
            if (variable.getUpperBounds().length == 1) {
                genericType = variable.getUpperBounds()[0];
            }
        }

        return genericType instanceof Class ? (Class<?>) genericType : Unknown.class;
    }

    private static Map<TypeVariable<?>, Type> getTypeVariableMap(final Class<?> targetType,
        Class<?> functionalInterface) {
        Map<TypeVariable<?>, Type> map = new HashMap<TypeVariable<?>, Type>();

        // Populate interfaces
        populateSuperTypeArgs(targetType.getGenericInterfaces(), map, functionalInterface != null);

        // Populate super classes and interfaces
        Type genericType = targetType.getGenericSuperclass();
        Class<?> type = targetType.getSuperclass();
        while (type != null && !Object.class.equals(type)) {
            if (genericType instanceof ParameterizedType) {
                populateTypeArgs((ParameterizedType) genericType, map, false);
            }
            populateSuperTypeArgs(type.getGenericInterfaces(), map, false);

            genericType = type.getGenericSuperclass();
            type = type.getSuperclass();
        }

        // Populate enclosing classes
        type = targetType;
        while (type.isMemberClass()) {
            genericType = type.getGenericSuperclass();
            if (genericType instanceof ParameterizedType) {
                populateTypeArgs((ParameterizedType) genericType, map, functionalInterface != null);
            }

            type = type.getEnclosingClass();
        }

        return map;
    }

    /**
     * Populates the {@code map} with with variable/argument pairs for the given {@code types}.
     */
    private static void populateSuperTypeArgs(final Type[] types, final Map<TypeVariable<?>, Type> map,
        boolean depthFirst) {
        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                if (!depthFirst) {
                    populateTypeArgs(parameterizedType, map, depthFirst);
                }
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class) {
                    populateSuperTypeArgs(((Class<?>) rawType).getGenericInterfaces(), map, depthFirst);
                }
                if (depthFirst) {
                    populateTypeArgs(parameterizedType, map, depthFirst);
                }
            } else if (type instanceof Class) {
                populateSuperTypeArgs(((Class<?>) type).getGenericInterfaces(), map, depthFirst);
            }
        }
    }

    /**
     * Populates the {@code map} with variable/argument pairs for the given {@code type}.
     */
    private static void populateTypeArgs(ParameterizedType type, Map<TypeVariable<?>, Type> map, boolean depthFirst) {
        if (type.getRawType() instanceof Class) {
            TypeVariable<?>[] typeVariables = ((Class<?>) type.getRawType()).getTypeParameters();
            Type[] typeArguments = type.getActualTypeArguments();

            if (type.getOwnerType() != null) {
                Type owner = type.getOwnerType();
                if (owner instanceof ParameterizedType) {
                    populateTypeArgs((ParameterizedType) owner, map, depthFirst);
                }
            }

            for (int i = 0; i < typeArguments.length; i++) {
                TypeVariable<?> variable = typeVariables[i];
                Type typeArgument = typeArguments[i];

                if (typeArgument instanceof Class) {
                    map.put(variable, typeArgument);
                } else if (typeArgument instanceof GenericArrayType) {
                    map.put(variable, typeArgument);
                } else if (typeArgument instanceof ParameterizedType) {
                    map.put(variable, typeArgument);
                } else if (typeArgument instanceof TypeVariable) {
                    TypeVariable<?> typeVariableArgument = (TypeVariable<?>) typeArgument;
                    if (depthFirst) {
                        Type existingType = map.get(variable);
                        if (existingType != null) {
                            map.put(typeVariableArgument, existingType);
                            continue;
                        }
                    }

                    Type resolvedType = map.get(typeVariableArgument);
                    if (resolvedType == null) {
                        resolvedType = resolveBound(typeVariableArgument);
                    }
                    map.put(variable, resolvedType);
                }
            }
        }
    }

    /**
     * Resolves the first bound for the {@code typeVariable}, returning {@code Unknown.class} if none can be resolved.
     */
    private static Type resolveBound(TypeVariable<?> typeVariable) {
        Type[] bounds = typeVariable.getBounds();
        if (bounds.length == 0) {
            return Unknown.class;
        }

        Type bound = bounds[0];
        if (bound instanceof TypeVariable) {
            bound = resolveBound((TypeVariable<?>) bound);
        }

        return bound == Object.class ? Unknown.class : bound;
    }

    /**
     * An unknown type.
     */
    private static final class Unknown {
    }

    private GenericUtils() {
    }
}
