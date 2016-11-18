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
package org.neo4j.ogm.config;

import java.util.Arrays;

import org.neo4j.ogm.autoindex.AutoIndexMode;

/**
 * Represents the configuration for Auto Index.
 * TODO: can we just consolidate all configuration into one class? It's getting unwieldy to keep adding subconfiguration objects.
 *
 * @author Mark Angrish
 */
public class AutoIndexConfiguration {

	private static final String[] AUTO_INDEX = {"neo4j.ogm.indexes.auto", "indexes.auto"};
	private static final String[] GENERATED_INDEXES_OUTPUT_DIR = {"neo4j.ogm.indexes.auto.dump.dir", "indexes.auto.dump.dir"};
	private static final String[] GENERATED_INDEXES_OUTPUT_FILENAME = {"neo4j.ogm.indexes.auto.dump.filename", "indexes.auto.dump.filename"};


	private static final AutoIndexMode DEFAULT_AUTO_INDEX_VALUE = AutoIndexMode.NONE;
	private static final String DEFAULT_GENERATED_INDEXES_FILENAME = "generated_indexes.cql";
	public static final String DEFAULT_GENERATED_INDEXES_DIR = ".";

	private final Configuration configuration;

	public AutoIndexConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public AutoIndexConfiguration setAutoIndex(String value) {

		if (AutoIndexMode.fromString(value) == null) {
			throw new RuntimeException("Invalid index value: " + value + ". Value must be one of: " + Arrays.toString(AutoIndexMode.values()));
		}

		configuration.set(AUTO_INDEX[0], value);
		return this;
	}

	public AutoIndexMode getAutoIndex() {
		if (configuration.get(AUTO_INDEX) == null) {
			return DEFAULT_AUTO_INDEX_VALUE;
		}
		return AutoIndexMode.fromString((String) configuration.get(AUTO_INDEX));
	}


	public AutoIndexConfiguration setDumpDir(String dumpDir) {
		configuration.set(GENERATED_INDEXES_OUTPUT_DIR[0], dumpDir);
		return this;
	}

	public String getDumpDir() {
		if (configuration.get(GENERATED_INDEXES_OUTPUT_DIR) == null) {
			return DEFAULT_GENERATED_INDEXES_DIR;
		}
		return (String) configuration.get(GENERATED_INDEXES_OUTPUT_DIR);
	}

	public AutoIndexConfiguration setDumpFilename(String dumpFilename) {
		configuration.set(GENERATED_INDEXES_OUTPUT_FILENAME[0], dumpFilename);
		return this;
	}

	public String getDumpFilename() {
		if (configuration.get(GENERATED_INDEXES_OUTPUT_FILENAME) == null) {
			return DEFAULT_GENERATED_INDEXES_FILENAME;
		}
		return (String) configuration.get(GENERATED_INDEXES_OUTPUT_FILENAME);
	}
}
