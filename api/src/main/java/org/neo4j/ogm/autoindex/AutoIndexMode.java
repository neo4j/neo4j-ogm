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
package org.neo4j.ogm.autoindex;

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
	 *
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
