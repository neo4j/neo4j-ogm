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

package org.neo4j.ogm.cypher;

import static org.junit.Assert.*;

import org.junit.Test;


public class RegExpTemplatePropertyValueTransformerTest {

    @Test
    public void shouldTransformCaseInsensitiveLike() throws Exception {

        PropertyValueTransformer transformer = new RegExpTemplatePropertyValueTransformer("(?i)%s")
                .replaceAll("\\*", ".*");
        assertEquals("(?i)San Francisco International.*",
                transformer.transformPropertyValue("San Francisco International*"));
    }

    @Test
    public void shouldTransformStartsWith() throws Exception {

        PropertyValueTransformer transformer = new RegExpTemplatePropertyValueTransformer("^%s.*");
        assertEquals("^San Francisco International.*",
                transformer.transformPropertyValue("San Francisco International"));
    }

}