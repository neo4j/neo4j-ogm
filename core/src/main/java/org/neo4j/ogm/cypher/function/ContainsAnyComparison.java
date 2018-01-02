/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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
