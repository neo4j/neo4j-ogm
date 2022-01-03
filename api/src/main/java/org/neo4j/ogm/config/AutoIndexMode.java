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
package org.neo4j.ogm.config;

/**
 * Denotes the types of auto indexing that can be done by the OGM at startup.
 *
 * @author Mark Angrish
 */
public enum AutoIndexMode {
    /**
     * No indexing will be performed.
     */
    NONE("none"),

    /**
     * Removes all indexes and constraints on startup then creates all indexes and constraints defined in metadata.
     */
    ASSERT("assert"),

    /**
     * Creates all missing indexes and constraints.
     * <p>
     * If there is an index in the database and constraint in the metadata the index is dropped and constraint created.
     * (and vise versa).
     * <p>
     * Other indexes and constraints are left untouched.
     * <p>
     * NOTE: When a field with index or constraint is renamed new index or constraint will be created.
     * Existing for the old name will be left untouched.
     */
    UPDATE("update"),

    /**
     * Ensures that all constraints and indexes exist on startup or will throw a Runtime exception.
     */
    VALIDATE("validate"),

    /**
     * Runs validate then creates a file (in same dir where launched) with the cypher used to build indexes and constraints.
     */
    DUMP("dump");

    /**
     * Parses an option name into the Enumeration type it represents.
     *
     * @param name The lowercase name to parse.
     * @return The <code>AutoIndexMode</code> this name represents.
     */
    public static AutoIndexMode fromString(String name) {
        if (name != null) {
            for (AutoIndexMode mode : AutoIndexMode.values()) {
                if (name.equalsIgnoreCase(mode.name)) {
                    return mode;
                }
            }
        }
        return null;
    }

    private final String name;

    AutoIndexMode(String name) {

        this.name = name;
    }

    public String getName() {
        return name;
    }
}
