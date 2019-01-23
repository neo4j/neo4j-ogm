/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.testutil;

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

    /**
     * Opens a driver on the first use to get the servers version.
     *
     * @return
     */
    public ServerVersion getServerVersion() {
        ServerVersion rv = serverVersion;
        if (rv == null) {
            synchronized (this) {
                rv = serverVersion;
                if (rv == null) {
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
