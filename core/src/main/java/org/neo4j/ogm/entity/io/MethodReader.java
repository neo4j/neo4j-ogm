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


import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.MethodInfo;
import org.neo4j.ogm.metadata.ObjectAnnotations;

import java.util.Map;

/**
 * @author Adam George
 * @author Luanne Misquitta
 */
public class MethodReader implements RelationalReader, PropertyReader {

    private final ClassInfo classInfo;
    private final MethodInfo methodInfo;

    MethodReader(ClassInfo classInfo, MethodInfo methodInfo) {
        this.classInfo = classInfo;
        this.methodInfo = methodInfo;
    }

    @Override
    public Object read(Object instance) {
        return MethodWriter.read(classInfo.getMethod(methodInfo), instance);
    }

    @Override
    public Object readProperty(Object instance) {
        if (methodInfo.hasCompositeConverter()) {
            throw new IllegalStateException(
                    "The readComposite method should be used for fields with a CompositeAttributeConverter");
        }
        Object value = MethodWriter.read(classInfo.getMethod(methodInfo), instance);
        if (methodInfo.hasPropertyConverter()) {
            value = methodInfo.getPropertyConverter().toGraphProperty(value);
        }
        return value;
    }

    @Override
    public Map<String, ?> readComposite(Object instance) {
        if (!methodInfo.hasCompositeConverter()) {
            throw new IllegalStateException(
                    "readComposite should only be used when a field is annotated with a CompositeAttributeConverter");
        }
        Object value = MethodWriter.read(classInfo.getMethod(methodInfo), instance);
        return methodInfo.getCompositeConverter().toGraphProperties(value);
    }

    @Override
    public String relationshipType() {
        return methodInfo.relationship();
    }

    @Override
    public String relationshipDirection() {
        ObjectAnnotations annotations = methodInfo.getAnnotations();
        if (annotations != null) {
            AnnotationInfo relationshipAnnotation = annotations.get(Relationship.CLASS);
            if (relationshipAnnotation != null) {
                return relationshipAnnotation.get(Relationship.DIRECTION, Relationship.UNDIRECTED);
            }
        }
        return Relationship.UNDIRECTED;
    }

    @Override
    public boolean isComposite() {
        return methodInfo.hasCompositeConverter();
    }

    @Override
    public String typeDescriptor() {
        return methodInfo.getTypeDescriptor();
    }

    @Override
    public String propertyName() {
        return methodInfo.property();
    }

}
