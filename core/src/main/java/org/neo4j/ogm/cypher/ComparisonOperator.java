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
package org.neo4j.ogm.cypher;

/**
 * Comparison operators used in queries.
 *
 * @author Luanne Misquitta
 * @author Adam George
 * @author Jasper Blues
 */
public enum ComparisonOperator {
    EQUALS("="),
    MATCHES("=~"),
    LIKE("=~", new CaseInsensitiveLikePropertyValueTransformer()),
    GREATER_THAN(">"),
    GREATER_THAN_EQUAL(">="),
    LESS_THAN("<"),
    LESS_THAN_EQUAL("<="),
    IS_NULL("IS NULL"),
    STARTING_WITH("STARTS WITH"),
    ENDING_WITH("ENDS WITH"),
    CONTAINING("CONTAINS"),
    IN("IN"),
    EXISTS("EXISTS"),
    IS_TRUE("=");

    private final String value;
    private final PropertyValueTransformer valueTransformer;

    ComparisonOperator(String value) {
        this(value, new NoOpPropertyValueTransformer());
    }

    ComparisonOperator(String value, PropertyValueTransformer propertyValueTransformer) {
        this.value = value;
        this.valueTransformer = propertyValueTransformer;
    }

    /**
     * @return The textual comparison operator to use in the Cypher query
     */
    public String getValue() {
        return value;
    }

    /**
     * @param these
     * @return
     * @deprecated since 3.2.3, no replacement, use an {@link java.util.EnumSet}
     */
    @Deprecated
    public boolean isOneOf(ComparisonOperator... these) {
        for (ComparisonOperator candidate : these) {
            if (this.equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return The {@link PropertyValueTransformer} required for this {@link ComparisonOperator} to work
     */
    public PropertyValueTransformer getPropertyValueTransformer() {
        return valueTransformer;
    }

}
