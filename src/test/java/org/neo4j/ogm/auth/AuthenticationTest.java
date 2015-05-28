/*
 * Copyright (c)  [2011-2015] "Neo Technology" / "Graph Aware Ltd."
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.auth;

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.kernel.Version;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.testutil.TestUtils;
import org.neo4j.server.NeoServer;
import org.neo4j.server.helpers.CommunityServerBuilder;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

/**
 * @author Vince Bickers
 */
public class AuthenticationTest
{
    private static NeoServer neoServer;
    private static int neoPort;
    private Session session;

    private boolean AUTH = true;
    private boolean NO_AUTH = false;

    @BeforeClass
    public static void setUp() {

        neoPort = TestUtils.getAvailablePort();
        try {
            neoServer = CommunityServerBuilder.server()
                    .withProperty("dbms.security.auth_enabled", "true")
                    .withProperty("dbms.security.auth_store.location", "src/test/resources/neo4j.credentials")
                    .onPort(neoPort).build();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    neoServer.stop();
                }
            });

            neoServer.start();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testUnauthorizedSession() {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        init( NO_AUTH, "org.neo4j.ogm.domain.bike" );

        try ( Transaction tx = session.beginTransaction() ) {
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

    // good enough for now: ignore test if we are not on something better than 2.1
    private boolean isRunningWithNeo4j2Dot2OrLater() {
        BigDecimal version = new BigDecimal(Version.getKernelRevision().substring(0,3));
        return version.compareTo(new BigDecimal("2.1")) > 0;
    }

    @Test
    public void testAuthorizedSession() {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        init(AUTH, "org.neo4j.ogm.domain.bike");

        try ( Transaction ignored = session.beginTransaction() ) {
            session.loadAll(Bike.class);
        } catch (ResultProcessingException rpe) {
            fail("'" + rpe.getCause().getLocalizedMessage() + "' was not expected here");
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

        session = new SessionFactory(packages).openSession(neoServer.baseUri().toString());

    }

}
