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


package org.neo4j.ogm.cypher.function;

import static org.neo4j.ogm.cypher.ComparisonOperator.*;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.cypher.Filter;

/**
 * @author Jasper Blues
 */
public class PropertyComparison implements FilterFunction<Object> {

    private Object value;
    private Filter filter;

    public PropertyComparison(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return this.value;
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
    public String expression(String nodeIdentifier) {
        if (filter.getComparisonOperator().equals(IS_NULL)) {
            return String.format("%s.`%s` IS NULL ", nodeIdentifier, filter.getPropertyName());
        } else if (filter.getComparisonOperator().equals(EXISTS)) {
            return String.format("EXISTS(%s.`%s`) ", nodeIdentifier, filter.getPropertyName());
        } else if (filter.getComparisonOperator().equals(IS_TRUE)) {
            return String.format("%s.`%s` = true ", nodeIdentifier, filter.getPropertyName());
        } else {
            return String.format("%s.`%s` %s { `%s` } ", nodeIdentifier, filter.getPropertyName(),
                    filter.getComparisonOperator().getValue(), filter.uniqueParameterName());
        }
    }

    @Override
    public Map<String, Object> parameters() {
        Map<String, Object> map = new HashMap<>();
        if (!filter.getComparisonOperator().isOneOf(IS_NULL, EXISTS, IS_TRUE)) {
            map.put(filter.uniqueParameterName(), filter.getTransformedPropertyValue());
        }
        return map;
    }
}
