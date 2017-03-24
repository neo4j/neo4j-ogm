/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic configuration class that can be set up programmatically
 * or via a properties file.
 *
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class Configuration {

	private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

	private String uri;
	private int connectionPoolSize;
	private String encryptionLevel;
	private String trustStrategy;
	private String trustCertFile;
	private AutoIndexMode autoIndex;
	private String generatedIndexesOutputDir;
	private String generatedIndexesOutputFilename;
	private String neo4jHaPropertiesFile;
	private String driverName;
	private Credentials credentials;


	Configuration(Builder builder) {
		this.uri = builder.uri;
		this.connectionPoolSize = builder.connectionPoolSize != null ? builder.connectionPoolSize : 50;
		this.encryptionLevel = builder.encryptionLevel;
		this.trustStrategy = builder.trustStrategy;
		this.trustCertFile = builder.trustCertFile;
		this.autoIndex = builder.autoIndex != null ? AutoIndexMode.fromString(builder.autoIndex) : AutoIndexMode.NONE;
		this.generatedIndexesOutputDir = builder.generatedIndexesOutputDir != null ? builder.generatedIndexesOutputDir : ".";
		this.generatedIndexesOutputFilename = builder.generatedIndexesOutputFilename != null ? builder.generatedIndexesOutputFilename : "generated_indexes.cql";
		this.neo4jHaPropertiesFile = builder.neo4jHaPropertiesFile;

		if (this.uri != null) {
			java.net.URI uri = null;
			try {
				uri = new URI(this.uri);
			} catch (URISyntaxException e) {
				throw new RuntimeException("Could not configure supplied URI in Configuration");
			}
			String userInfo = uri.getUserInfo();
			if (userInfo != null) {
				String[] userPass = userInfo.split(":");
				credentials = new UsernamePasswordCredentials(userPass[0], userPass[1]);
				this.uri = uri.toString().replace(uri.getUserInfo() + "@", "");
			}
			if (getDriverClassName() == null) {
				determineDefaultDriverName(uri.getScheme());
			}
		} else {
			determineDefaultDriverName("file");
		}
		assert this.driverName != null;

		if (builder.username != null && builder.password != null) {
			if (this.credentials != null) {
				LOGGER.warn("Overriding credentials supplied in URI with supplied username and password.");
			}
			credentials = new UsernamePasswordCredentials(builder.username, builder.password);
		}

	}

	public AutoIndexMode getAutoIndex() {
		return autoIndex;
	}

	public String getDumpDir() {
		return generatedIndexesOutputDir;
	}

	public String getDumpFilename() {
		return generatedIndexesOutputFilename;
	}

	public String getURI() {
		return uri;
	}

	public String getDriverClassName() {
		return driverName;
	}

	public int getConnectionPoolSize() {
		return connectionPoolSize;
	}

	public String getEncryptionLevel() {
		return encryptionLevel;
	}

	public String getTrustStrategy() {
		return trustStrategy;
	}

	public String getTrustCertFile() {
		return trustCertFile;
	}

	public String getNeo4jHaPropertiesFile() {
		return neo4jHaPropertiesFile;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	private void determineDefaultDriverName(String scheme) {
		switch (scheme) {
			case "http":
			case "https":
				this.driverName = "org.neo4j.ogm.drivers.http.driver.HttpDriver";
				break;
			case "bolt":
				this.driverName = "org.neo4j.ogm.drivers.bolt.driver.BoltDriver";
				break;
			default:
				this.driverName = "org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver";
				break;
		}
	}

	public static class Builder {

		private static final String URI = "URI";
		private static final String CONNECTION_POOL_SIZE = "connection.pool.size";
		private static final String ENCRYPTION_LEVEL = "encryption.level";
		private static final String TRUST_STRATEGY = "trust.strategy";
		private static final String TRUST_CERT_FILE = "trust.certificate.file";
		private static final String AUTO_INDEX = "indexes.auto";
		private static final String GENERATED_INDEXES_OUTPUT_DIR = "indexes.auto.dump.dir";
		private static final String GENERATED_INDEXES_OUTPUT_FILENAME = "indexes.auto.dump.filename";
		private static final String NEO4J_HA_PROPERTIES_FILE = "neo4j.ha.properties.file";

		private String uri;
		private Integer connectionPoolSize;
		private String encryptionLevel;
		private String trustStrategy;
		private String trustCertFile;
		private String autoIndex;
		private String generatedIndexesOutputDir;
		private String generatedIndexesOutputFilename;
		private String neo4jHaPropertiesFile;
		private String username;
		private String password;

		public Builder() {
		}

		public Builder(ConfigurationSource configurationSource) {
			for (Map.Entry<Object, Object> entry : configurationSource.properties().entrySet()) {
				switch (entry.getKey().toString()) {
					case URI:
						this.uri = (String) entry.getValue();
						break;
					case CONNECTION_POOL_SIZE:
						this.connectionPoolSize = Integer.parseInt((String) entry.getValue());
						break;
					case ENCRYPTION_LEVEL:
						this.encryptionLevel = (String) entry.getValue();
						break;
					case TRUST_STRATEGY:
						this.trustStrategy = (String) entry.getValue();
						break;
					case TRUST_CERT_FILE:
						this.trustCertFile = (String) entry.getValue();
						break;
					case AUTO_INDEX:
						this.autoIndex = (String) entry.getValue();
						break;
					case GENERATED_INDEXES_OUTPUT_DIR:
						this.generatedIndexesOutputDir = (String) entry.getValue();
						break;
					case GENERATED_INDEXES_OUTPUT_FILENAME:
						this.generatedIndexesOutputFilename = (String) entry.getValue();
						break;
					case NEO4J_HA_PROPERTIES_FILE:
						this.neo4jHaPropertiesFile = (String) entry.getValue();
						break;
					default:
						LOGGER.warn("Could not process property with key: {}", entry.getKey());
				}
			}
		}

		public Builder uri(String uri) {
			this.uri = uri;
			return this;
		}

		public Builder connectionPoolSize(Integer connectionPoolSize) {
			this.connectionPoolSize = connectionPoolSize;
			return this;
		}

		public Builder encryptionLevel(String encryptionLevel) {
			this.encryptionLevel = encryptionLevel;
			return this;
		}

		public Builder trustStrategy(String trustStrategy) {
			this.trustStrategy = trustStrategy;
			return this;
		}

		public Builder trustCertFile(String trustCertFile) {
			this.trustCertFile = trustCertFile;
			return this;
		}

		public Builder autoIndex(String autoIndex) {
			this.autoIndex = autoIndex;
			return this;
		}

		public Builder generatedIndexesOutputDir(String generatedIndexesOutputDir) {
			this.generatedIndexesOutputDir = generatedIndexesOutputDir;
			return this;
		}

		public Builder generatedIndexesOutputFilename(String generatedIndexesOutputFilename) {
			this.generatedIndexesOutputFilename = generatedIndexesOutputFilename;
			return this;
		}

		public Builder neo4jHaPropertiesFile(String neo4jHaPropertiesFile) {
			this.neo4jHaPropertiesFile = neo4jHaPropertiesFile;
			return this;
		}

		public Configuration build() {
			return new Configuration(this);
		}

		public Builder credentials(String username, String password) {
			this.username = username;
			this.password = password;
			return this;
		}
	}
}
