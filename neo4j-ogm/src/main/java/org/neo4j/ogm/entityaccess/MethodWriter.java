/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.entityaccess;

import org.neo4j.ogm.metadata.ClassUtils;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;

import java.lang.reflect.Method;

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
        }
        MethodWriter.write(method, instance, value);
    }

    @Override
    public Class<?> type() {
        return parameterType;
    }

    @Override
    public String relationshipName() {
        return this.setterMethodInfo.relationship();
    }

    @Override
    public String relationshipDirection() {
        return setterMethodInfo.relationshipDirection();
    }

}
