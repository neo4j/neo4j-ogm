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

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.neo4j.driver.v1.exceptions.ServiceUnavailableException;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
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

        try (ServerControls serverControls = TestServerBuilders.newInProcessBuilder().newServer()) {

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
