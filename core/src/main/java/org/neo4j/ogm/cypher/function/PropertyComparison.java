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


package org.neo4j.ogm.cypher.function;

import org.neo4j.ogm.cypher.Filter;

import java.util.HashMap;
import java.util.Map;

public class PropertyComparison implements FilterFunction<Object> {

    private Object value;

    public PropertyComparison(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String cypherFragment(Filter filter, String nodeIdentifier) {
        String uniquePropertyName = filter.isNested() ?
                filter.getNestedPropertyName() + "_" + filter.getPropertyName() : filter.getPropertyName();
        return String.format("%s.`%s` %s { `%s` } ", nodeIdentifier, filter.getPropertyName(),
                filter.getComparisonOperator().getValue(), uniquePropertyName);
    }

    @Override
    public Map<String, Object> cypherProperties(Filter filter) {
        Map<String, Object> map = new HashMap<>();
        map.put(filter.uniquePropertyName(), filter.getTransformedPropertyValue());
        return map;
    }

}
