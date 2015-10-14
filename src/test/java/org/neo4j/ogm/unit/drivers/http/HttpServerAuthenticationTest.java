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

package org.neo4j.ogm.unit.drivers.http;

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.kernel.Version;
import org.neo4j.ogm.api.transaction.Transaction;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.driver.impl.result.ResultProcessingException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.AuthenticatingTestServer;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class HttpServerAuthenticationTest {

    private static AuthenticatingTestServer testServer;

    private Session session;
    private boolean AUTH = true;
    private boolean NO_AUTH = false;
    private String DOMAIN_CLASSES = "org.neo4j.ogm.bike";

    @BeforeClass
    public static void setUp() {
        testServer = new AuthenticatingTestServer();
    }

    @Test
    public void testUnauthorizedSession() {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        init(NO_AUTH, DOMAIN_CLASSES);

        try (Transaction tx = session.beginTransaction()) {
            session.loadAll(Bike.class);
            fail("A non-authenticating version of Neo4j is running. Please start Neo4j 2.2.0 or later to run these tests");
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

    // good enough for now: ignore test if we are not on something better than 2.1
    private boolean isRunningWithNeo4j2Dot2OrLater() {
        BigDecimal version = new BigDecimal(Version.getKernelRevision().substring(0, 3));
        return version.compareTo(new BigDecimal("2.1")) > 0;
    }

    @Test
    public void testAuthorizedSession() {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        init(AUTH, DOMAIN_CLASSES);

        try (Transaction ignored = session.beginTransaction()) {
            session.loadAll(Bike.class);
        } catch (ResultProcessingException rpe) {
            fail("'" + rpe.getCause().getLocalizedMessage() + "' was not expected here");
        }

    }

    /**
     * @see issue #35
     */
    @Test
    public void testAuthorizedSessionWithSuppliedCredentials() {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        initWithSuppliedCredentials("neo4j", "password", DOMAIN_CLASSES);

        try (Transaction ignored = session.beginTransaction()) {
            session.loadAll(Bike.class);
        } catch (ResultProcessingException rpe) {
            fail("'" + rpe.getCause().getLocalizedMessage() + "' was not expected here");
        }

    }

    /**
     * @see issue #35
     */
    @Test
    public void testUnauthorizedSessionWithSuppliedCredentials() {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        initWithSuppliedCredentials("neo4j", "incorrectPassword", DOMAIN_CLASSES);

        try (Transaction tx = session.beginTransaction()) {
            session.loadAll(Bike.class);
            fail("A non-authenticating version of Neo4j is running. Please start Neo4j 2.2.0 or later to run these tests");
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

    /**
     * @see issue #35
     */
    @Test
    public void testAuthorizedSessionWithURI() throws URISyntaxException {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        URI uri = new URI(testServer.url());

        initWithEmbeddedCredentials("http://neo4j:password@" + uri.getHost() + ":" + uri.getPort(), DOMAIN_CLASSES);

        try (Transaction ignored = session.beginTransaction()) {
            session.loadAll(Bike.class);
        } catch (ResultProcessingException rpe) {
            fail("'" + rpe.getCause().getLocalizedMessage() + "' was not expected here");
        }

    }

    /**
     * @see issue #35
     */
    @Test
    public void testUnauthorizedSessionWithURI() {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        initWithEmbeddedCredentials(testServer.url(), DOMAIN_CLASSES);

        try (Transaction tx = session.beginTransaction()) {
            session.loadAll(Bike.class);
            fail("A non-authenticating version of Neo4j is running. Please start Neo4j 2.2.0 or later to run these tests");
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

    private void init(boolean auth, String... packages) {

        if (auth) {
            System.setProperty("username", "neo4j");
            System.setProperty("password", "password");
        } else {
            System.getProperties().remove("username");
            System.getProperties().remove("password");
        }

        session = new SessionFactory(packages).openSession(testServer.url());

    }

    private void initWithSuppliedCredentials(String username, String password, String... packages) {
        System.getProperties().remove("username");
        System.getProperties().remove("password");

        session = new SessionFactory(packages).openSession(testServer.url(), username, password);
    }

    private void initWithEmbeddedCredentials(String url, String... packages) {
        System.getProperties().remove("username");
        System.getProperties().remove("password");

        session = new SessionFactory(packages).openSession(url);
    }
}
