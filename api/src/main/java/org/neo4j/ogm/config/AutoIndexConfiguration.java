package org.neo4j.ogm.config;

import org.neo4j.ogm.index.AutoIndexMode;

/**
 * Created by markangrish on 16/09/2016.
 */
public class AutoIndexConfiguration {

	private static final String[] AUTO_INDEX = {"neo4j.ogm.indexes.auto", "indexes.auto"};
	private static final AutoIndexMode DEFAULT_AUTO_INDEX_VALUE = AutoIndexMode.NONE;

	private final Configuration configuration;

	public AutoIndexConfiguration() {
		this.configuration = new Configuration();
	}

	public AutoIndexConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public AutoIndexConfiguration setAutoIndex(String value) {

		if (AutoIndexMode.fromString(value) == null) {
			throw new RuntimeException("Invalid index value: " + value + ". Value must be one of: " + AutoIndexMode.stringValues());
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
}
