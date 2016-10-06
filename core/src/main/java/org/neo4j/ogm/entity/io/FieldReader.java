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

package org.neo4j.ogm.entity.io;


import java.util.Map;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.ObjectAnnotations;

/**
 * @author Adam George
 * @author Luanne Misquitta
 */
public class FieldReader implements RelationalReader, PropertyReader {

    private final ClassInfo classInfo;
    private final FieldInfo fieldInfo;

    public FieldReader(ClassInfo classInfo, FieldInfo fieldInfo) {
        this.classInfo = classInfo;
        this.fieldInfo = fieldInfo;
    }

    @Override
    public Object read(Object instance) {
        return FieldWriter.read(classInfo.getField(fieldInfo), instance);
    }

    @Override
    public Object readProperty(Object instance) {
        if (fieldInfo.hasCompositeConverter()) {
            throw new IllegalStateException(
                    "The readComposite method should be used for fields with a CompositeAttributeConverter");
        }
        Object value = FieldWriter.read(classInfo.getField(fieldInfo), instance);
        if (fieldInfo.hasPropertyConverter()) {
            value = fieldInfo.getPropertyConverter().toGraphProperty(value);
        }
        return value;
    }

    @Override
    public Map<String, ?> readComposite(Object instance) {
        if (!fieldInfo.hasCompositeConverter()) {
            throw new IllegalStateException(
                    "readComposite should only be used when a field is annotated with a CompositeAttributeConverter");
        }
        Object value = FieldWriter.read(classInfo.getField(fieldInfo), instance);
        return fieldInfo.getCompositeConverter().toGraphProperties(value);
    }

    @Override
    public String relationshipType() {
        return fieldInfo.relationship();
    }

    @Override
    public String propertyName() {
        return fieldInfo.property();
    }

    @Override
    public boolean isComposite() {
        return fieldInfo.hasCompositeConverter();
    }

    @Override
    public String relationshipDirection() {
        ObjectAnnotations annotations = fieldInfo.getAnnotations();
        if (annotations != null) {
            AnnotationInfo relationshipAnnotation = annotations.get(Relationship.CLASS);
            if (relationshipAnnotation != null) {
                return relationshipAnnotation.get(Relationship.DIRECTION, Relationship.UNDIRECTED);
            }
        }
        return Relationship.UNDIRECTED;
    }

    @Override
    public String typeDescriptor() {
        return fieldInfo.getTypeDescriptor();
    }
}
