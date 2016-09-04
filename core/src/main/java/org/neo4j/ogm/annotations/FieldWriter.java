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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.neo4j.ogm.utils.ClassUtils;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.session.Utils;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class FieldWriter extends EntityAccess {

    private final FieldInfo fieldInfo;
    private final Field field;
    private final Class<?> fieldType;

    public FieldWriter(ClassInfo classInfo, FieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
        this.field = classInfo.getField(fieldInfo);
        this.fieldType = this.field.getType();
    }

    public static void write(Field field, Object instance, Object value) {
        try {
            field.setAccessible(true);
            field.set(instance, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object read(Field field, Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(Object instance, Object value) {

        if (fieldInfo.hasPropertyConverter()) {
            value = fieldInfo.getPropertyConverter().toEntityAttribute(value);
            FieldWriter.write(field, instance, value);
        }

        else {
            if (fieldInfo.isScalar()) {
                String descriptor = fieldInfo.getTypeParameterDescriptor() == null ? fieldInfo.getDescriptor()
                        : fieldInfo.getTypeParameterDescriptor();
                value = Utils.coerceTypes(ClassUtils.getType(descriptor), value);
            }
            FieldWriter.write(field, instance, value);
        }
    }

    @Override
    public Class<?> type() {
        Class convertedType = fieldInfo.convertedType();
        if (convertedType != null) {
            return convertedType;
        }
        return fieldType;
    }

    @Override
    public String relationshipName() {
        return this.fieldInfo.relationship();
    }

    @Override
    public String relationshipDirection() {
        return fieldInfo.relationshipDirection(Relationship.UNDIRECTED);
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
        return fieldInfo.getTypeDescriptor();
    }
}
