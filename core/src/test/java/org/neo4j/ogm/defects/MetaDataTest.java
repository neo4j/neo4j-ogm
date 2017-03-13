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

package org.neo4j.ogm.defects;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.ClassInfo;

import static org.junit.Assert.*;

/**
 * @author Luanne Misquitta
 */
public class MetaDataTest {

    private MetaData metaData;

    @Before
    public void setUp() {
        metaData = new MetaData("org.neo4j.ogm.domain.forum", "org.neo4j.ogm.domain.canonical", "org.neo4j.ogm.integration.hierarchy.domain", "org.neo4j.ogm.domain.cineasts.annotated");
    }


    @Test
    public void testThatJavaDotLangDotEnumIsAnEnum() {
        ClassInfo classInfo = metaData.classInfo("Enum");
        assertNotNull("I was expecting java.lang.Enum to be in the meta-data", classInfo);
        assertEquals("java.lang.Enum", classInfo.name());
        assertTrue("Surely java.lang.Enum should be considered an enum", classInfo.isEnum());
    }

}
