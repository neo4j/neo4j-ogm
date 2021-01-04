/*
 * Copyright (c) 2002-2021 "Neo4j,"
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

import java.util.Map;
import java.util.function.UnaryOperator;

import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.PropertyValueTransformer;

/**
 * @author Jasper Blues
 * @author Michael J. Simons
 */
public interface FilterFunction<T> {

    /**
     * @return The filter used
     * @deprecated since 3.2.3, no replacement.
     */
    @Deprecated
    Filter getFilter();

    /**
     * @param filter The filter to use
     * @deprecated since 3.2.3, no replacement.
     */
    @Deprecated
    void setFilter(Filter filter);

    T getValue();

    /**
     * Generates a cypher expresion for this function
     * @param nodeIdentifier The identifier of the node to be filtered in the query
     * @return The fragment to use
     * @deprecated since 3.2.3, use {@link #expression(String, String, UnaryOperator)}
     */
    @Deprecated
    String expression(String nodeIdentifier);

    /**
     * Generates a cypher expression for this function
     * @param nodeIdentifier The identifier of the node to be filtered in the query
     * @param filteredProperty The identifier of the filtered property
     * @param createUniqueParameterName An operator to create unique parameter names, the same as in {@link #parameters(UnaryOperator, PropertyValueTransformer)}
     * @return The fragment to use
     */
    default String expression(String nodeIdentifier, String filteredProperty, UnaryOperator<String> createUniqueParameterName) {
        return expression(nodeIdentifier);
    }

    /**
     * @return The parameters for this filter
     * @deprecated since 3.2.3, use {@link #parameters(UnaryOperator, PropertyValueTransformer)}
     */
    @Deprecated
    Map<String, Object> parameters();

    /**
     * Provides the map of parameters to use. It is advised to use the provided operator for creating unique parameter names
     * @param createUniqueParameterName An operator to create unique parameter names
     * @param valueTransformer Transformer for adapting possible values to the domain
     * @return The map of parameters
     */
    default Map<String, Object> parameters(UnaryOperator<String> createUniqueParameterName, PropertyValueTransformer valueTransformer) {
        return parameters();
    }
}
