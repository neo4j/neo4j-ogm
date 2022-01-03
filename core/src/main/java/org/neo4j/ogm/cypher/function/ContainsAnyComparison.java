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
package org.neo4j.ogm.cypher.function;

import java.util.Collections;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.neo4j.ogm.cypher.PropertyValueTransformer;

/**
 * Filter to allow searching in collection properties.
 * The difference with the IN filter is that the IN has a single parameter value, whereas
 * ContainsAny value can be a collection and will match against properties
 * containing at least one of these values.
 * For example
 * <pre>
 *  Filter f = new Filter("specialities", new ContainsAnyComparison(Arrays.asList("burger", "sushi")));
 *  Collection&lt;Restaurant&gt; all = session.loadAll(Restaurant.class, new Filters(f));
 * </pre>
 * will match all restaurant having burger OR sushi as a speciality.
 *
 * @author Gerrit Meier
 * @author Michael J. Simons
 */
public class ContainsAnyComparison implements FilterFunction<Object> {

    protected static final String PARAMETER_NAME = "property";

    private final Object value;

    public ContainsAnyComparison(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String expression(String nodeIdentifier, String filteredProperty,
        UnaryOperator<String> createUniqueParameterName) {

        return String.format("ANY(collectionFields IN $`%s` WHERE collectionFields in %s.`%s`) ",
            createUniqueParameterName.apply(PARAMETER_NAME), nodeIdentifier, filteredProperty);
    }

    @Override
    public Map<String, Object> parameters(UnaryOperator<String> createUniqueParameterName,
        PropertyValueTransformer propertyValueTransformer) {

        return Collections.singletonMap(createUniqueParameterName.apply(PARAMETER_NAME),
            propertyValueTransformer.transformPropertyValue(this.value));
    }
}
