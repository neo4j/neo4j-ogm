/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
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
