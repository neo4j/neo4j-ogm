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

package org.neo4j.ogm.persistence.authentication;


import static org.junit.Assert.*;
import static org.junit.Assume.*;

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.config.Components;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */

public class AuthenticatingDriverTest extends MultiDriverTestClass {

    private Session session;

    @Before
    public void beforeMethod() {
        assumeTrue(Components.driver() instanceof HttpDriver);
    }

    @Test
    public void testUnauthorizedDriver() {

        baseConfiguration.setCredentials("", "");
        session = new SessionFactory(baseConfiguration, "dummy").openSession();

        try (Transaction tx = session.beginTransaction()) {
            fail("Driver should not have authenticated");
        } catch (Exception rpe) {
            Throwable cause = rpe.getCause();
            if (cause instanceof HttpHostConnectException) {
                fail("Please start Neo4j 2.2.0 or later to run these tests");
            } else {
                while (!(cause instanceof HttpResponseException)) {
                    cause = cause.getCause();
                }
                assertTrue(cause.getMessage().startsWith("Invalid username or password"));
            }
        }
    }

    @Test
    public void testAuthorizedDriver() {

        session = new SessionFactory(baseConfiguration, "dummy").openSession();

        try (Transaction ignored = session.beginTransaction()) {
            assertNotNull(ignored);
        } catch (Exception rpe) {
            fail("'" + rpe.getLocalizedMessage() + "' was not expected here");
        }
    }

    /**
     * @see issue #35
     */
    @Test
    public void testInvalidCredentials() {

        baseConfiguration.setCredentials("neo4j", "invalid_password");
        session = new SessionFactory(baseConfiguration, "dummy").openSession();

        try (Transaction tx = session.beginTransaction()) {
            fail("Driver should not have authenticated");
        } catch (Exception rpe) {
            Throwable cause = rpe.getCause();
            if (cause instanceof HttpHostConnectException) {
                fail("Please start Neo4j 2.2.0 or later to run these tests");
            } else {
                while (!(cause instanceof HttpResponseException)) {
                    cause = cause.getCause();
                }
                assertEquals("Invalid username or password.", cause.getMessage());
            }
        }
    }
}
