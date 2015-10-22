/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm.core.authentication;


import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.api.authentication.UsernamePasswordCredentials;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.service.Components;
import org.neo4j.ogm.api.transaction.Transaction;
import org.neo4j.ogm.core.session.Session;
import org.neo4j.ogm.core.session.SessionFactory;
import org.neo4j.ogm.core.testutil.AuthenticatingTestServer;
import org.neo4j.ogm.exception.ResultProcessingException;

import static org.junit.Assert.*;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */

public class AuthenticatingServerTest {

    private Driver driver;
    private AuthenticatingTestServer testServer;
    private Session session;

    @Before
    public void setUp() {
        driver = Components.driver();
        testServer= new AuthenticatingTestServer(driver);
    }

    @After
    public void tearDown() {
        testServer.shutdown();
    }

    @Test
    public void testUnauthorizedDriver() {

        driver.setConfig("credentials", null);

        session = new SessionFactory("dummy").openSession(driver);

        try (Transaction tx = session.beginTransaction()) {
            fail("Driver should not have authenticated");
        } catch (ResultProcessingException rpe) {
            Throwable cause = rpe.getCause();
            if (cause instanceof HttpHostConnectException) {
                fail("Please start Neo4j 2.2.0 or later to run these tests");
            } else {
                assertTrue(cause instanceof HttpResponseException);
                assertEquals("Unauthorized", cause.getMessage());
            }
        } finally {

        }
    }

    @Test
    public void testAuthorizedDriver() {

        driver.setConfig("credentials", new UsernamePasswordCredentials("neo4j", "password"));

        session = new SessionFactory("dummy").openSession(driver);

        try (Transaction ignored = session.beginTransaction()) {
            assertNotNull(ignored);
        } catch (ResultProcessingException rpe) {
            fail("'" + rpe.getCause().getLocalizedMessage() + "' was not expected here");
        }

    }

    /**
     * @see issue #35
     */
    @Test
    public void testInvalidCredentials() {

        driver.setConfig("credentials", new UsernamePasswordCredentials("neo4j", "invalid_password"));

        session = new SessionFactory("dummy").openSession(driver);

        try (Transaction tx = session.beginTransaction()) {
            fail("Driver should not have authenticated");
        } catch (ResultProcessingException rpe) {
            Throwable cause = rpe.getCause();
            if (cause instanceof HttpHostConnectException) {
                fail("Please start Neo4j 2.2.0 or later to run these tests");
            } else {
                assertTrue(cause instanceof HttpResponseException);
                assertEquals("Unauthorized", cause.getMessage());
            }
        }

    }


}
