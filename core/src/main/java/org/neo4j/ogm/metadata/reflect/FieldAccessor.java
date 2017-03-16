/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices and license terms. Your use of the source code for these subcomponents is subject to the terms and conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 *
 */

package org.neo4j.ogm.metadata.reflect;

import java.lang.reflect.Field;
import java.util.Map;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.ObjectAnnotations;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.utils.ClassUtils;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class FieldAccessor {

    private final ClassInfo classInfo;
    private final FieldInfo fieldInfo;
    private final Field field;
    private final Class<?> fieldType;

    public FieldAccessor(ClassInfo classInfo, FieldInfo fieldInfo) {
        this.classInfo = classInfo;
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
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void write(Object instance, Object value) {

        if (fieldInfo.hasPropertyConverter()) {
            value = fieldInfo.getPropertyConverter().toEntityAttribute(value);
            FieldAccessor.write(field, instance, value);
        } else {
            if (fieldInfo.isScalar()) {
                String descriptor = fieldInfo.getTypeDescriptor();
                value = Utils.coerceTypes(ClassUtils.getType(descriptor), value);
            }
            FieldAccessor.write(field, instance, value);
        }
    }

    public Class<?> type() {
        Class convertedType = fieldInfo.convertedType();
        if (convertedType != null) {
            return convertedType;
        }
        return fieldType;
    }

    public String relationshipName() {
        return this.fieldInfo.relationship();
    }

    public boolean forScalar() {
        return !Iterable.class.isAssignableFrom(type()) && !type().isArray();
    }

    public String typeParameterDescriptor() {
        return fieldInfo.getTypeDescriptor();
    }

    public Object read(Object instance) {
        return FieldAccessor.read(classInfo.getField(fieldInfo), instance);
    }

    public Object readProperty(Object instance) {
        if (fieldInfo.hasCompositeConverter()) {
            throw new IllegalStateException(
                    "The readComposite method should be used for fields with a CompositeAttributeConverter");
        }
        Object value = FieldAccessor.read(classInfo.getField(fieldInfo), instance);
        if (fieldInfo.hasPropertyConverter()) {
            value = fieldInfo.getPropertyConverter().toGraphProperty(value);
        }
        return value;
    }

    public Map<String, ?> readComposite(Object instance) {
        if (!fieldInfo.hasCompositeConverter()) {
            throw new IllegalStateException(
                    "readComposite should only be used when a field is annotated with a CompositeAttributeConverter");
        }
        Object value = FieldAccessor.read(classInfo.getField(fieldInfo), instance);
        return fieldInfo.getCompositeConverter().toGraphProperties(value);
    }

    public String relationshipType() {
        return fieldInfo.relationship();
    }

    public String propertyName() {
        return fieldInfo.property();
    }

    public boolean isComposite() {
        return fieldInfo.hasCompositeConverter();
    }

    public String relationshipDirection() {
        ObjectAnnotations annotations = fieldInfo.getAnnotations();
        if (annotations != null) {
            AnnotationInfo relationshipAnnotation = annotations.get(Relationship.class);
            if (relationshipAnnotation != null) {
                return relationshipAnnotation.get(Relationship.DIRECTION, Relationship.UNDIRECTED);
            }
        }
        return Relationship.UNDIRECTED;
    }

    public String typeDescriptor() {
        return fieldInfo.getTypeDescriptor();
    }
}
