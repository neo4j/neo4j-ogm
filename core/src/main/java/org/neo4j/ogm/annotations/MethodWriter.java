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

package org.neo4j.ogm.annotations;

import java.lang.reflect.Method;

import org.neo4j.ogm.utils.ClassUtils;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MethodInfo;
import org.neo4j.ogm.session.Utils;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class MethodWriter extends EntityAccess {

    private final MethodInfo setterMethodInfo;
    private final Class<?> parameterType;
    private final Method method;

    MethodWriter(ClassInfo classInfo, MethodInfo methodInfo) {
        this.setterMethodInfo = methodInfo;
        this.parameterType = ClassUtils.getType(setterMethodInfo.getDescriptor());
        this.method = classInfo.getMethod(setterMethodInfo, parameterType);
    }

    private static void write(Method method, Object instance, Object value) {
        try {
            method.invoke(instance, value);
        } catch (IllegalArgumentException iae) {
            throw new EntityAccessException("Failed to invoke method '" + method.getName() + "'. Expected argument type: " + method.getParameterTypes()[0] + " actual argument type: " + value.getClass(), iae);
        } catch (Exception e) {
            throw new EntityAccessException("Failed to invoke method '" + method.getName() + "'", e);
        }

    }

    public static Object read(Method method, Object instance) {
        try {
            return method.invoke(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Object instance, Object value) {

        if (setterMethodInfo.hasConverter()) {
            value = setterMethodInfo.converter().toEntityAttribute(value);
            MethodWriter.write(method, instance, value);
        }

        else {
            if (setterMethodInfo.isScalar()) {
                String descriptor = setterMethodInfo.getTypeParameterDescriptor() == null ? setterMethodInfo.getDescriptor() : setterMethodInfo.getTypeParameterDescriptor();
                value = Utils.coerceTypes(ClassUtils.getType(descriptor), value);
            }
            MethodWriter.write(method, instance, value);
        }
    }

    @Override
    public Class<?> type() {
        if (setterMethodInfo.hasConverter()) {
            try {
                for(Method method : setterMethodInfo.converter().getClass().getDeclaredMethods()) {
                    if(method.getName().equals("toGraphProperty") && !method.isSynthetic()) { //we don't want the method on the AttributeConverter interface
                        return method.getReturnType();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return parameterType;
    }

    @Override
    public String relationshipName() {
        return this.setterMethodInfo.relationship();
    }

    @Override
    public String relationshipDirection() {
        return setterMethodInfo.relationshipDirection(Relationship.UNDIRECTED);
    }

    @Override
    public boolean forScalar() {
        if (Iterable.class.isAssignableFrom(type())) {
            return false;
        }
        if (type().isArray()) {
            return false;
        }
        return true;
    }

    @Override
    public String typeParameterDescriptor() {
        return setterMethodInfo.getTypeDescriptor();
    }
}
