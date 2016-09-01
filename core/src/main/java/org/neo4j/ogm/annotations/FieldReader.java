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


import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.metadata.AnnotationInfo;
import org.neo4j.ogm.metadata.ClassInfo;
import org.neo4j.ogm.metadata.FieldInfo;
import org.neo4j.ogm.metadata.ObjectAnnotations;

import java.util.Map;

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
        Object value = FieldWriter.read(classInfo.getField(fieldInfo), instance);
        if (fieldInfo.hasPropertyConverter()) {
            value = fieldInfo.getPropertyConverter().toGraphProperty(value);
        }
        else if (fieldInfo.hasCompositeConverter()) {
            value = fieldInfo.getCompositeConverter().toGraphProperties(value);
        }
        return value;
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
    public String relationshipDirection() {
        ObjectAnnotations annotations = fieldInfo.getAnnotations();
        if(annotations != null) {
            AnnotationInfo relationshipAnnotation = annotations.get(Relationship.CLASS);
            if(relationshipAnnotation != null) {
                return relationshipAnnotation.get(Relationship.DIRECTION, Relationship.UNDIRECTED);
            }
        }
        return Relationship.UNDIRECTED;
    }

    @Override
    public String typeParameterDescriptor() {
       return fieldInfo.getTypeDescriptor();
    }
}
