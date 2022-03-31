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

import static org.neo4j.ogm.cypher.ComparisonOperator.*;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.UnaryOperator;

import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.PropertyValueTransformer;

/**
 * @author Jasper Blues
 * @author Michael J. Simons
 */
public class PropertyComparison implements FilterFunction<Object> {

    protected static final String PARAMETER_NAME = "property";

    protected final ComparisonOperator operator;
    protected final Object value;

    public PropertyComparison(ComparisonOperator operator, Object value) {
        this.operator = operator;
        this.value = value;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }

    @Override
    public Object getValue() {
        return this.value;
    }

    @Override
    public String expression(String nodeIdentifier, String filteredProperty,
        UnaryOperator<String> createUniqueParameterName) {

        if (operator == IS_NULL) {
            return String.format("%s.`%s` IS NULL ", nodeIdentifier, filteredProperty);
        } else if (operator == EXISTS) {
            return String.format("%s.`%s` IS NOT NULL ", nodeIdentifier, filteredProperty);
        } else if (operator == IS_TRUE) {
            return String.format("%s.`%s` = true ", nodeIdentifier, filteredProperty);
        } else {
            return String.format("%s.`%s` %s $`%s` ", nodeIdentifier, filteredProperty,
                operator.getValue(), createUniqueParameterName.apply(PARAMETER_NAME));
        }
    }

    @Override
    public Map<String, Object> parameters(UnaryOperator<String> createUniqueParameterName,
        PropertyValueTransformer valueTransformer) {
        if (EnumSet.of(IS_NULL, EXISTS, IS_TRUE).contains(operator)) {
            return Collections.emptyMap();
        } else {
            return Collections.singletonMap(createUniqueParameterName.apply(PARAMETER_NAME),
                valueTransformer.andThen(this.operator.getPropertyValueTransformer())
                    .transformPropertyValue(this.value));
        }
    }

    /**
     * Internal class for modifying an EQUALS or CONTAINS comparison to ignore the case of both attribute and parameter.
     */
    public static final class CaseInsensitiveEqualsComparison extends PropertyComparison {
        public CaseInsensitiveEqualsComparison(ComparisonOperator operator, Object value) {
            super(operator, value);
        }

        @Override
        public String expression(final String nodeIdentifier, String filteredProperty,
            UnaryOperator<String> createUniqueParameterName) {
            return String.format("toLower(%s.`%s`) %s toLower($`%s`) ", nodeIdentifier, filteredProperty,
                operator.getValue(), createUniqueParameterName.apply(PARAMETER_NAME));
        }
    }
}
