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
package org.neo4j.ogm.exception;

/**
 * An exception raised when executing a Cypher query
 *
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class CypherException extends RuntimeException {

    private final String code;
    private final String description;

    /**
     * @param code
     * @param description
     * @since 3.1.6
     */
    public CypherException(String code, String description) {
        this(code, description, null);
    }

    /**
     * @param code        Error code / text as returned by the database.
     * @param description Description as returned by the database.
     * @param cause       The root cause.
     * @since 3.1.6
     */
    public CypherException(String code, String description, Throwable cause) {
        super(String.format("Cypher execution failed with code '%s': %s.", code, description), cause);
        this.code = code;
        this.description = description;
    }

    /**
     * The Neo4j error status code
     *
     * @return the neo4j error status code
     */
    public String getCode() {
        return code;
    }

    /**
     * The error description
     *
     * @return the error description
     */
    public String getDescription() {
        return description;
    }
}
