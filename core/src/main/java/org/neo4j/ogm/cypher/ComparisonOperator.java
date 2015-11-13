/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.cypher;

/**
 * Comparison operators used in queries.
 *
 * @author Luanne Misquitta
 * @author Adam George
 */
public enum ComparisonOperator {
	EQUALS("="),
	MATCHES("=~"),
	LIKE("=~", new CaseInsensitiveLikePropertyValueTransformer()),
	GREATER_THAN(">"),
	LESS_THAN("<");

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
     * @return The {@link PropertyValueTransformer} required for this {@link ComparisonOperator} to work
     */
    public PropertyValueTransformer getPropertyValueTransformer() {
        return valueTransformer;
    }

}
