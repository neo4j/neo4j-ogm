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
package org.neo4j.ogm.cypher;

/**
 * Allows a property value to be transformed into a certain format for use with particular {@link ComparisonOperator}s
 * when building a Cypher query.
 *
 * @author Adam George
 * @author Michael J. Simons
 */
@FunctionalInterface
public interface PropertyValueTransformer {

    /**
     * Transforms the given property value into a format that's compatible with the comparison operator in the context
     * of the current query being built.
     *
     * @param propertyValue The property value to transform, which may be <code>null</code>
     * @return The transformed property value or <code>null</code> if invoked with <code>null</code>
     */
    Object transformPropertyValue(Object propertyValue);

    /**
     * Applies this transformer first, then {@literal after} as the next transformation.
     *
     * @param after The next transformation.
     * @return A new property value transformer.
     */
    default PropertyValueTransformer andThen(PropertyValueTransformer after) {

        return (t) -> after.transformPropertyValue(this.transformPropertyValue(t));
    }

}
