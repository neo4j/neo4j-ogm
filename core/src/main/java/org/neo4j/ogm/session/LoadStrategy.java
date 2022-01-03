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
package org.neo4j.ogm.session;

/**
 * @author Frantisek Hartman
 */
public enum LoadStrategy {

    /**
     * Load strategy which fetches related nodes by querying all paths from matched nodes, resulting into pattern
     * similar to
     * {@code MATCH p=(n)-[*0..n]-() RETURN p}
     */
    PATH_LOAD_STRATEGY,

    /**
     * Load strategy which uses nested list comprehensions to get related nodes based on the schema generated from
     * entity classes
     * NOTE: Does not support queries with unlimited depth
     */
    SCHEMA_LOAD_STRATEGY;
}
