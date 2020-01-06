/*
 * Copyright (c) 2002-2020 "Neo4j,"
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

package org.neo4j.ogm.metadata.schema;

/**
 * Relationship in a {@link Schema}
 *
 * @author Frantisek Hartman
 */
public interface Relationship {

    /**
     * Type of this relationship
     */
    String type();

    /**
     * Direction of the relationship from given node
     *
     * @return direction
     */
    String direction(Node node);

    /**
     * Return start node of this relationship
     *
     * @return start node
     */
    Node start();

    /**
     * Return the other node on this side of the relationship
     *
     * @param node node
     * @return the other node
     */
    Node other(Node node);
}
