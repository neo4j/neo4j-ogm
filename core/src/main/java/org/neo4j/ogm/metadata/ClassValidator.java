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

package org.neo4j.ogm.metadata;

import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.exception.MappingException;

public class ClassValidator {

    private ClassMetadata classInfo;


    public ClassValidator(ClassMetadata classInfo) {
        this.classInfo = classInfo;
    }

    public void validate() throws MappingException {
        validateRelationshipEntity();
        validateFields();
    }

    private void validateRelationshipEntity() throws MappingException {
        if (classInfo.isRelationshipEntity() && classInfo.labelFieldOrNull() != null) {
            throw new MappingException(String.format("'%s' is a relationship entity. The @Labels annotation can't be applied to " +
                    "relationship entities.", classInfo.name()));
        }
    }

    private void validateFields() throws MappingException {
        for (FieldMetadata fieldInfo : classInfo.fieldsInfo().fields()) {
            if (fieldInfo.hasAnnotation(Property.class) && fieldInfo.hasCompositeConverter()) {
                throw new MappingException(String.format("'%s' has both @Convert and @Property annotations applied to the field '%s'",
                        classInfo.name(), fieldInfo.getName()));
            }
        }
    }
}
