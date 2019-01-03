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
package org.neo4j.ogm.drivers.bolt.driver;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.CypherExceptionTestBase;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Michael J. Simons
 */
public class BoltCypherExceptionTest extends CypherExceptionTestBase<BoltDriver> {

    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void initSessionFactory() {
        Driver driver = new BoltDriver(
            GraphDatabase.driver(serverControls.boltURI(), Config.build().withoutEncryption().toConfig()));
        sessionFactory = new SessionFactory(driver, DOMAIN_PACKAGE);
    }

    @Override
    protected SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @AfterClass
    public static void closeSessionFactory() {
        sessionFactory.close();
    }
}
