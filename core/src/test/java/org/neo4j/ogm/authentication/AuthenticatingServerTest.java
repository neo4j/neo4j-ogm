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

package org.neo4j.ogm.authentication;


import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.DriverConfiguration;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.service.DriverService;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.AuthenticatingTestServer;
import org.neo4j.ogm.transaction.Transaction;

import static org.junit.Assert.*;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */

public class AuthenticatingServerTest {

    private AuthenticatingTestServer testServer;
    private Session session;

    @Before
    public void setUp() {
        testServer= new AuthenticatingTestServer(new HttpDriver(new DriverConfiguration()));
    }

    @After
    public void tearDown() {
        testServer.shutdown();
    }

    @Test
    public void testUnauthorizedDriver() {

        testServer.driver().getConfiguration().setCredentials(null);
        session = new SessionFactory("dummy").openSession(testServer.driver());

        try (Transaction tx = session.beginTransaction()) {
            fail("Driver should not have authenticated");
        } catch (Exception rpe) {
            Throwable cause = rpe.getCause();
            if (cause instanceof HttpHostConnectException) {
                fail("Please start Neo4j 2.2.0 or later to run these tests");
            } else {
                while (cause instanceof HttpResponseException == false) {
                    cause = cause.getCause();
                }
                assertEquals("Unauthorized", cause.getMessage());
            }
        }
    }

    @Test
    public void testAuthorizedDriver() {

        session = new SessionFactory("dummy").openSession(testServer.driver());

        try (Transaction ignored = session.beginTransaction()) {
            assertNotNull(ignored);
        } catch (Exception rpe) {
            fail("'" + rpe.getCause().getLocalizedMessage() + "' was not expected here");
        }

    }

    /**
     * @see issue #35
     */
    @Test
    public void testInvalidCredentials() {

        testServer.driver().getConfiguration().setCredentials("neo4j", "invalid_password");
        session = new SessionFactory("dummy").openSession(testServer.driver());

        try (Transaction tx = session.beginTransaction()) {
            fail("Driver should not have authenticated");
        } catch (Exception rpe) {
            Throwable cause = rpe.getCause();
            if (cause instanceof HttpHostConnectException) {
                fail("Please start Neo4j 2.2.0 or later to run these tests");
            } else {
                while (cause instanceof HttpResponseException == false) {
                    cause = cause.getCause();
                }
                assertEquals("Unauthorized", cause.getMessage());
            }
        }
    }


}
