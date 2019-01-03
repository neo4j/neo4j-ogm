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
package org.neo4j.ogm.metadata.schema;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Node in a {@link Schema}
 *
 * @author Frantisek Hartman
 */
public interface Node {

    /**
     * Primary - the most specific - label of the node
     *
     * @return label
     */
    Optional<String> label();

    /**
     * Labels this node has, usually only 1
     *
     * @return labels
     */
    Collection<String> labels();

    /**
     * Relationships declared on this node
     * The key in the map is the a name of the relationship, not type. E.g. a field name in the class
     *
     * @return relationship
     */
    Map<String, Relationship> relationships();

}
