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

import org.junit.Test;
import org.neo4j.ogm.MetaData;
import org.neo4j.ogm.exception.MappingException;
import org.neo4j.ogm.invalid.convert.InvalidDiner;
import org.neo4j.ogm.invalid.labels.method.LabelsAnnotationOnGettersAndSetters;

import static org.junit.Assert.*;


public class ClassValidatorTest {

    /**
     * @see issue #159
     */
    @Test
    public void throwsExceptionWhenLabelAnnotationOnMethods() {
        try {
            MetaData metaData = new MetaData("org.neo4j.ogm.invalid.labels.method");
            metaData.classInfo(LabelsAnnotationOnGettersAndSetters.class.getSimpleName());
            fail("Should have thrown exception.");
        } catch (MappingException e) {
            assertTrue(e.getMessage().startsWith("'org.neo4j.ogm.invalid.labels.method.LabelsAnnotationOnGettersAndSetters' has the @Labels annotation applied to"));
        }
    }

    /**
     * @see issue #159
     */
    @Test
    public void throwsExceptionWhenLabelAnnotationWithRelationshipEntity() {
        try {
            MetaData metaData = new MetaData("org.neo4j.ogm.invalid.labels.relationship");
            metaData.classInfo(LabelsAnnotationOnGettersAndSetters.class.getSimpleName());
            fail("Should have thrown exception.");
        } catch (MappingException e) {
            assertEquals("'org.neo4j.ogm.invalid.labels.relationship.LabelsAnnotationRelationshipEntity' is a relationship entity. The @Labels annotation can't be applied to relationship entities.", e.getMessage());
        }
    }

    @Test
    public void throwsExceptionWhenConvertAnnotionOnMethods() {
        try {
            MetaData metaData = new MetaData("org.neo4j.ogm.invalid.convert");
            metaData.classInfo(InvalidDiner.class.getSimpleName());
            fail("Should have thrown exception.");
        } catch (MappingException e) {
            assertTrue(e.getMessage().startsWith("'org.neo4j.ogm.invalid.convert.InvalidDiner' has the @Convert annotation applied to method 'setLocation'"));
        }
    }


}