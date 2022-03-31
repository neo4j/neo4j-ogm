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
package org.neo4j.ogm.autoindex;

import java.util.EnumSet;

/**
 * @author Frantisek Hartman
 */
enum IndexType {

    /**
     * Single property index
     */
    SINGLE_INDEX,

    /**
     * Composite index
     */
    COMPOSITE_INDEX,

    /**
     * Unique constraint
     */
    UNIQUE_CONSTRAINT,

    /**
     *
     */
    NODE_KEY_CONSTRAINT,

    /**
     * Node property existence constraint
     */
    NODE_PROP_EXISTENCE_CONSTRAINT,

    /**
     * Relationship property existence constraint
     */
    REL_PROP_EXISTENCE_CONSTRAINT;

    boolean isConstraint() {
        return EnumSet.of(UNIQUE_CONSTRAINT, NODE_KEY_CONSTRAINT, NODE_PROP_EXISTENCE_CONSTRAINT,
            REL_PROP_EXISTENCE_CONSTRAINT).contains(this);
    }
}
