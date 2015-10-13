/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.unit.mapper;

import static org.junit.Assert.*;

import org.junit.Test;
import org.neo4j.ogm.domain.education.School;
import org.neo4j.ogm.domain.education.Teacher;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.metadata.MetaData;

/**
 * @author Vince Bickers
 */
public class ObjectMemoTest {

    private static final MetaData metaData = new MetaData("org.neo4j.ogm.domain.education");
    private static final MappingContext mappingContext = new MappingContext(metaData);

    @Test
    public void testUnchangedObjectDetected() {
        Teacher mrsJones = new Teacher();


        mrsJones.setId(115L); // the id field must not be part of the memoised property list
        mappingContext.remember(mrsJones);
        assertFalse(mappingContext.isDirty(mrsJones));

    }

    @Test
    public void testChangedPropertyDetected() {
        Teacher teacher = new Teacher("Miss White");

        teacher.setId(115L); // the id field must not be part of the memoised property list
        mappingContext.remember(teacher);

        teacher.setName("Mrs Jones"); // the teacher's name property has changed.
        assertTrue(mappingContext.isDirty(teacher));
    }

    @Test
    public void testRelatedObjectChangeDoesNotAffectNodeMemoisation() {
        Teacher teacher = new Teacher("Miss White");


        teacher.setId(115L); // the id field must not be part of the memoised property list
        mappingContext.remember(teacher);

        teacher.setSchool(new School("Roedean")); // a related object does not affect the property list.

        assertFalse(mappingContext.isDirty(teacher));
    }



}
