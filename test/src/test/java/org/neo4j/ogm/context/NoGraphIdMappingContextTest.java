/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

package org.neo4j.ogm.context;

import org.junit.Before;
import org.junit.Test;

import org.neo4j.ogm.metadata.MetaData;

/**
 * @author Frantisek Hartman
 */
public class NoGraphIdMappingContextTest {

    private MappingContext context;

    @Before
    public void setUp() {
        context = new MappingContext(new MetaData("org.neo4j.ogm.domain.drink"));
    }

    @Test
    public void shouldHaveMappingContext() throws Exception {

    }
}
