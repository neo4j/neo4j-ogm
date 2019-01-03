/*
 * Copyright (c) 2002-2019 "Neo4j,"
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

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.cypher.Filter;

/**
 * Filter to allow searching in collection properties.
 * The difference with the IN filter is that the IN has a single parameter value, whereas
 * ContainsAny value can be a collection and will match against properties
 * containing at least one of these values.
 * For example
 * <pre>
 *  Filter f = new Filter("specialities", new ContainsAnyComparison(Arrays.asList("burger", "sushi")));
 *  Collection<Restaurant> all = session.loadAll(Restaurant.class, new Filters(f));
 * </pre>
 * will match all restaurant having burger OR sushi as a speciality.
 */
public class ContainsAnyComparison implements FilterFunction<Object> {

    private final Object value;
    private Filter filter;

    public ContainsAnyComparison(Object value) {
        this.value = value;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public String expression(String nodeIdentifier) {
        return String.format("ANY(collectionFields IN {`%s`} WHERE collectionFields in %s.`%s`) ",
            filter.uniqueParameterName(), nodeIdentifier, filter.getPropertyName());
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> map = new HashMap<>();
        map.put(filter.uniqueParameterName(), filter.getTransformedPropertyValue());
        return map;
    }
}
