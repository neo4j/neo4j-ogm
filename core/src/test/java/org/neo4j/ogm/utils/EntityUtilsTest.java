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

package org.neo4j.ogm.utils;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.pizza.Pizza;
import org.neo4j.ogm.metadata.MetaData;


public class EntityUtilsTest {

    private MetaData metaData;

    @Before
    public void setUp() {
        metaData = new MetaData("org.neo4j.ogm.domain.pizza");
    }

    @Test
    public void shouldCollateStaticAndRuntimeLabels() throws Exception {

        Pizza pizza = new Pizza();

        List<String> labels = new ArrayList<>();
        labels.add("Delicious");
        labels.add("Hot");
        labels.add("Spicy");
        pizza.setLabels(labels);

        Collection<String> collatedLabels = EntityUtils.labels(pizza, metaData);
        assertNotNull(collatedLabels);
        assertTrue(collatedLabels.contains("Delicious"));
        assertTrue(collatedLabels.contains("Hot"));
        assertTrue(collatedLabels.contains("Spicy"));
        assertTrue(collatedLabels.contains("Pizza"));
    }
}
