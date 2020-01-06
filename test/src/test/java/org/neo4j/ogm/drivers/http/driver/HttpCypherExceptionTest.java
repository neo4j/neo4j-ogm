/*
 * Copyright (c) 2002-2020 "Neo4j,"
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
package org.neo4j.ogm.drivers.http.driver;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.CypherExceptionTestBase;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Michael J. Simons
 */
public class HttpCypherExceptionTest extends CypherExceptionTestBase<HttpDriver> {

    private static SessionFactory sessionFactory;

    @BeforeClass
    public static void initSessionFactory() {
        Driver driver = new HttpDriver();
        driver.configure(new Configuration.Builder().uri(serverControls.httpURI().toString()).build());
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
