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

package org.neo4j.ogm.defects;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.metadata.info.MethodInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Luanne Misquitta
 */
@Ignore
public class MetaDataTest {

	private MetaData metaData;

	@Before
	public void setUp() {
		metaData = new MetaData("org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.canonical","org.neo4j.ogm.integration.hierarchy.domain","org.neo4j.ogm.domain.cineasts.annotated");
	}

    //@org.junit.Ignore("I do think we should implement this, but it's not really possible without loading classes")
    @Test
    public void testResolutionOfRelationshipTypeFromMethodInfo() {
        ClassInfo classInfo = metaData.resolve("Forum");
        assertNotNull("The resolved class info shouldn't be null", classInfo);
        assertEquals("org.neo4j.ogm.domain.forum.Forum", classInfo.name());

        final String relationshipType = "HAS_TOPIC";

        // test getters
        MethodInfo relationshipEntityGetter = classInfo.relationshipGetter(relationshipType);
        assertNotNull(relationshipEntityGetter);
        assertEquals(relationshipType, relationshipEntityGetter.relationship());

        // test setters
        MethodInfo relationshipEntitySetter = classInfo.relationshipSetter(relationshipType);
        assertNotNull(relationshipEntitySetter);
        assertEquals(relationshipType, relationshipEntitySetter.relationship());
    }

    @Test
    public void testThatJavaDotLangDotEnumIsAnEnum() {
        ClassInfo classInfo = metaData.classInfo("Enum");
        assertNotNull("I was expecting java.lang.Enum to be in the meta-data", classInfo);
        assertEquals("java.lang.Enum", classInfo.name());
        assertTrue("Surely java.lang.Enum should be considered an enum", classInfo.isEnum());
    }

}
