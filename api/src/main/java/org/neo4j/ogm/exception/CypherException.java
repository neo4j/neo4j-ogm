/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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
 * @author Luanne Misquitta
 */
public class CypherException extends RuntimeException {

	private String code;
	private String description;

	public CypherException(String message, Throwable cause, String code, String description) {
		super(message + "; Code: " + code + "; Description: " + description, cause);
		this.code = code;
		this.description = description;
	}

	public CypherException(String message, String code, String description) {
		super(message + "; Code: " + code + "; Description: " + description);
		this.code = code;
		this.description = description;
	}

	/**
	 * The Neo4j error status code
	 * @return the neo4j error status code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * The error description
	 * @return the error description
	 */
	public String getDescription() {
		return description;
	}
}
