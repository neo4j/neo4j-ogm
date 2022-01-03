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

/**
 * Load clause builder builds final part of load queries
 *
 * @author Frantisek Hartman
 */
public interface LoadClauseBuilder {

    /**
     * Build load clause based on given parameters, expecting the node variable to expand to be 'n'
     *
     * @param label label of the start node
     * @param depth max depth to load, note that some implementations may not accept unlimited depth (-1)
     * @return Cypher query as string
     */
    default String build(String label, int depth) {
        return build("n", label, depth);
    }

    /**
     * Build load clause based on given parameters
     *
     * @param variable node variable (start node) to be expanded
     * @param label    label of the start node
     * @param depth    max depth to load, note that some implementations may not accept unlimited depth (-1)
     * @return Cypher query as string
     */
    String build(String variable, String label, int depth);
}
