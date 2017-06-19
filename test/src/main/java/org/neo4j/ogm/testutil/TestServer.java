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

package org.neo4j.ogm.testutil;

import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.server.AbstractNeoServer;
import org.neo4j.server.database.Database;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Mark Angrish
 */
public class TestServer {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestServer.class);

	private final Integer port;
	private final Integer transactionTimeoutSeconds;
	private final Boolean enableAuthentication;
	private final Boolean enableBolt;

	private GraphDatabaseService database;
	private ServerControls controls;

	private String username;
	private String password;
	private String uri;

	public TestServer(boolean enableAuthentication, boolean enableBolt, int transactionTimeoutSeconds) {
		this(enableAuthentication, enableBolt, transactionTimeoutSeconds, TestUtils.getAvailablePort());
	}

	public TestServer(boolean enableAuthentication, boolean enableBolt, int transactionTimeoutSeconds, int port) {

		this.port = port;
		this.transactionTimeoutSeconds = transactionTimeoutSeconds;
		this.enableAuthentication = enableAuthentication;
		this.enableBolt = enableBolt;

		startServer();
		LOGGER.info("Starting {} server on: {}", enableBolt ? "BOLT": "HTTP", port);
	}

	public void startServer() {
		try {

			if (enableBolt) {
				controls = TestServerBuilders.newInProcessBuilder()
						.withConfig("dbms.connector.0.type", "BOLT")
						.withConfig("dbms.connector.0.enabled", "true")
						.withConfig("dbms.connector.0.listen_address", "localhost:" + String.valueOf(port))
						.newServer();
			} else {
				controls = TestServerBuilders.newInProcessBuilder()
						.withConfig("dbms.connector.1.type", "HTTP")
						.withConfig("dbms.connector.1.enabled", "true")
						.withConfig("dbms.connector.1.listen_address", "localhost:" + String.valueOf(port))
						.withConfig("dbms.security.auth_enabled", String.valueOf(enableAuthentication))
						.withConfig("org.neo4j.server.webserver.port", String.valueOf(port))
						.withConfig("org.neo4j.server.transaction.timeout", String.valueOf(transactionTimeoutSeconds))
						.withConfig("dbms.transaction_timeout", String.valueOf(transactionTimeoutSeconds))
						.withConfig("dbms.security.auth_store.location", createAuthStore())
						.withConfig("unsupported.dbms.security.auth_store.location", createAuthStore())
						.withConfig("remote_shell_enabled", "false")
						.newServer();
			}

			initialise(controls);

			// ensure we shutdown this server when the JVM terminates, if its not been shutdown by user code
			Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
		} catch (Exception e) {
			throw new RuntimeException("Error starting in-process server", e);
		}
	}

	private String createAuthStore() {
		// creates a temp auth store, with encrypted credentials "neo4j:password" if the server is authenticating connections
		try {
			Path authStore = Files.createTempFile("neo4j", "credentials");
			authStore.toFile().deleteOnExit();

			if (enableAuthentication) {
				try (Writer authStoreWriter = new FileWriter(authStore.toFile())) {
					IOUtils.write("neo4j:SHA-256,03C9C54BF6EEF1FF3DFEB75403401AA0EBA97860CAC187D6452A1FCF4C63353A,819BDB957119F8DFFF65604C92980A91:", authStoreWriter);
				}
				this.username = "neo4j";
				this.password = "password";
			}

			return authStore.toAbsolutePath().toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void initialise(ServerControls controls) throws Exception {
		setDatabase(controls);
		this.uri = url();
	}

	private void setDatabase(ServerControls controls) throws Exception {
		try {
			Method method = controls.getClass().getMethod("graph");
			database = (GraphDatabaseService) method.invoke(controls);
		} catch (NoSuchMethodException nsme) {
			Class clazz = Class.forName("org.neo4j.harness.internal.InProcessServerControls");
			Field field = clazz.getDeclaredField("server");
			field.setAccessible(true);
			AbstractNeoServer server = (AbstractNeoServer) field.get(controls);
			Database db = server.getDatabase();
			database = db.getGraph();
		}
	}

	/**
	 * Stops the underlying server bootstrapper and, in turn, the Neo4j server.
	 */
	public void shutdown() {

		if (database != null && database.isAvailable(100)) {
			LOGGER.info("Stopping {} server on: {}", enableBolt ? "BOLT": "HTTP", port);
			database.shutdown();
			database = null;
		}
		controls.close();
	}

	/**
	 * Waits for a period of time and checks the database availability afterwards
	 *
	 * @return true if the database is available, false otherwise
	 */
	boolean isRunning() {
		return database.isAvailable((long) 1000);
	}

	/**
	 * Retrieves the base URL of the Neo4j database server used in the test.
	 *
	 * @return The URL of the Neo4j test server
	 */
	private String url() {

		Method method;
		try {
			if (enableBolt) {
				method = controls.getClass().getMethod("boltURI");
			} else {
				method = controls.getClass().getMethod("httpURI");
			}
			Object url = method.invoke(controls);
			return url.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Retrieves the underlying {@link org.neo4j.graphdb.GraphDatabaseService} used in this test.
	 *
	 * @return The test {@link org.neo4j.graphdb.GraphDatabaseService}
	 */
	public GraphDatabaseService getGraphDatabaseService() {
		return this.database;
	}

	public String getUri() {
		return uri;
	}

	public Integer getPort() {
		return port;
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}
}
