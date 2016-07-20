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

import static org.junit.Assert.assertEquals;

/**
 * @author vince
 */
public class AnnotationsTest {


    @Test
    public void shouldLoadMetaDataWithComplexAnnotations() {

        MetaData metaData = new MetaData("org.neo4j.ogm.domain.annotations");

        assertEquals("org.neo4j.ogm.domain.annotations.SimpleNode", metaData.classInfo("SimpleNode").name());
        assertEquals("org.neo4j.ogm.domain.annotations.OtherNode", metaData.classInfo("OtherNode").name());

    }
}
