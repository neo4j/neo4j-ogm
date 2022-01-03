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
package org.neo4j.ogm.session.request.strategy;

import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.CypherQuery;

/**
 * @author vince
 */
public interface AggregateStatements {

    /**
     * construct queries to count all nodes with the specified label
     *
     * @param labels the labels attached to the object
     * @return a {@link CypherQuery}
     */
    CypherQuery countNodes(Iterable<String> labels);

    /**
     * construct queries to count all nodes with the specified label that match the specified filters
     *
     * @param label   the label value to filter on
     * @param filters additional parameters to filter on
     * @return a {@link CypherQuery}
     */

    CypherQuery countNodes(String label, Iterable<Filter> filters);

    /**
     * construct queries to count all relationships with the specified type that match the specified filters
     *
     * @param type    the relationship type to filter on
     * @param filters additional parameters to filter on
     * @return a {@link CypherQuery}
     */
    CypherQuery countEdges(String type, Iterable<Filter> filters);

    /**
     * construct queries to count all single-length paths with the specified start label, relationship type and end label that match the specified filters
     *
     * @param startLabel       the start node label to filter on
     * @param relationshipType the type of relationship to filter on
     * @param endLabel         the end node label to filter on
     * @return a {@link CypherQuery}
     */
    CypherQuery countEdges(String startLabel, String relationshipType, String endLabel);
}
