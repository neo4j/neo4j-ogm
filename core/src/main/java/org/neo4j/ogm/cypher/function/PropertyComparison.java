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
