/*
 * Copyright (c) 2002-2015 "Neo Technology"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and licence terms.  Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's licence, as noted in the LICENSE file.
 */
package org.neo4j.ogm.cypher;

/**
 * Allows a property value to be transformed into a certain format for use with particular {@link ComparisonOperator}s
 * when building a Cypher query.
 *
 * @author Adam George
 */
public interface PropertyValueTransformer {

    /**
     * Transforms the given property value into a format that's compatible with the comparison operator in the context
     * of the current query being built.
     *
     * @param propertyValue The property value to transform, which may be <code>null</code>
     * @return The transformed property value or <code>null</code> if invoked with <code>null</code>
     */
    Object transformPropertyValue(Object propertyValue);

}
