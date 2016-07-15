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

import org.neo4j.ogm.annotations.Labels;
import org.neo4j.ogm.exception.MappingException;

public class ClassValidator {

    private ClassInfo classInfo;


    public ClassValidator(ClassInfo classInfo) {
        this.classInfo = classInfo;
    }

    public void validate() throws MappingException {
        validateRelationshipEntity();
        validateMethods();
    }

    private void validateRelationshipEntity() throws MappingException {
        if (classInfo.isRelationshipEntity() && classInfo.labelFieldOrNull() != null) {
            throw new MappingException(String.format("'%s' is a relationship entity. The @Labels annotation can't be applied to " +
                    "relationship entities.", classInfo.name()));
        }
    }

    private void validateMethods() {
        for (MethodInfo methodInfo : classInfo.propertyGettersAndSetters()) {
            if (methodInfo.hasAnnotation(Labels.CLASS)) {
                throw new MappingException(String.format("'%s' has the @Labels annotation applied to method '%s'. " +
                                "The @Labels annotation can only be applied to a field.",
                        classInfo.name(), methodInfo.getName()));
            }
        }
    }

}
