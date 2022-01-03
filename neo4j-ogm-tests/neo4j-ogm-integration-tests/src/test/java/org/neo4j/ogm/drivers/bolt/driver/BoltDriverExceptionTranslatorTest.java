/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.drivers.bolt.driver;

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.neo4j.harness.Neo4j;
import org.neo4j.harness.Neo4jBuilders;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.domain.cypher_exception_test.ConstraintedNode;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

/**
 * @author Michael J. Simons
 */
public class BoltDriverExceptionTranslatorTest {

    protected static final String DOMAIN_PACKAGE = "org.neo4j.ogm.domain.cypher_exception_test";

    @Test
    public void translatesServiceUnavailabeException() {

        try (Neo4j serverControls = Neo4jBuilders.newInProcessBuilder().build()) {

            BoltDriver driver = new BoltDriver();
            Configuration ogmConfiguration = new Configuration.Builder().uri(serverControls.boltURI().toString())
                .verifyConnection(true).build();
            driver.configure(ogmConfiguration);

            SessionFactory sessionFactory = new SessionFactory(driver, DOMAIN_PACKAGE);
            Session session = sessionFactory.openSession();

            serverControls.close();

            assertThatExceptionOfType(ConnectionException.class).isThrownBy(() -> {
                session.loadAll(ConstraintedNode.class);
            }).withCauseInstanceOf(ServiceUnavailableException.class);
        }
    }
}
