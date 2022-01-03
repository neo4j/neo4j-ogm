/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.utils;

import static org.assertj.core.api.Assertions.*;

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
        assertThat(collatedLabels).isNotNull();
        assertThat(collatedLabels).containsOnly("Delicious", "Hot", "Spicy", "Pizza");
    }
}
