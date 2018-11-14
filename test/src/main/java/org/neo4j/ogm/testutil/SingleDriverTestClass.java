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
package org.neo4j.ogm.testutil;

import java.net.URI;
import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.driver.internal.util.ServerVersion;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.session.SessionFactory;

/**
 * In contrast to the {@link MultiDriverTestClass}, this facilitates only the Neo4j Test Harness and provides access to
 * a server through a configured Java driver respectively the embedded instanceof {@link #getGraphDatabaseService()}.<br>
 * In cases needed, it offers also the {@link org.neo4j.driver.internal.util.ServerVersion} of the backing database.
 *
 * @author Michael J. Simons
 */
public abstract class SingleDriverTestClass {
    static final Config DRIVER_CONFIG = Config.build().withoutEncryption().toConfig();

    private static ServerControls serverControls;
    private ServerVersion serverVersion;

    @BeforeClass
    public static void initializeNeo4j() {
        serverControls = TestServerBuilders.newInProcessBuilder().newServer();
    }

    public GraphDatabaseService getGraphDatabaseService() {
        return serverControls.graph();
    }

    /**
     * Gets a new driver against the server controls. The caller is required to close this driver instance himself.
     *
     * @return A ready to use driver
     */
    public Driver getDriver() {
        return GraphDatabase.driver(serverControls.boltURI(), DRIVER_CONFIG);
    }

    public URI getBoltURI() {
        return serverControls.boltURI();
    }

    /**
     * Opens a driver on the first use to get the servers version.
     *
     * @return
     */
    public ServerVersion getServerVersion() {
        ServerVersion rv = serverVersion;
        if (rv == null) {
            synchronized (this) {
                if (serverVersion == null) {
                    try (Driver driver = getDriver()) {
                        rv = serverVersion = ServerVersion.version(driver);
                    }
                }
            }
        }
        return rv;
    }

    public boolean databaseSupportJava8TimeTypes() {
        return getServerVersion()
            .greaterThanOrEqual(ServerVersion.version("3.4.0"));
    }

    protected void doWithSessionFactoryOf(
        AbstractConfigurableDriver ogmDriver, Class[] inBasePackageClasses,
        Consumer<SessionFactory> consumerOfSessionFactory
    ) {
        String[] basePackages = Arrays.stream(inBasePackageClasses)
            .map(Class::getName)
            .toArray(String[]::new);

        SessionFactory sessionFactory = null;
        try {
            sessionFactory = new SessionFactory(ogmDriver, basePackages);
            consumerOfSessionFactory.accept(sessionFactory);
        } finally {
            if (sessionFactory != null) {
                sessionFactory.close();
            }
        }
    }

    @AfterClass
    public static void tearDownNeo4j() {
        serverControls.close();
    }

}
