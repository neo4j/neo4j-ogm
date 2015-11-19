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

package org.neo4j.ogm.auth;

import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.FileWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;
import org.neo4j.harness.internal.InProcessServerControls;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.testutil.TestUtils;
import org.neo4j.server.AbstractNeoServer;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class AuthenticationTest
{
    private static AbstractNeoServer neoServer;
    private static int neoPort;
    private Session session;

    private boolean AUTH = true;
    private boolean NO_AUTH = false;

    @BeforeClass
    public static void setUp() throws Exception {

        Path authStore = Files.createTempFile( "neo4j", "credentials" );
        authStore.toFile().deleteOnExit();
        try (Writer authStoreWriter = new FileWriter( authStore.toFile() )) {
            IOUtils.write( "neo4j:SHA-256,03C9C54BF6EEF1FF3DFEB75403401AA0EBA97860CAC187D6452A1FCF4C63353A,819BDB957119F8DFFF65604C92980A91:", authStoreWriter );
        }

        neoPort = TestUtils.getAvailablePort();

        try {
            ServerControls controls = TestServerBuilders.newInProcessBuilder()
                    .withConfig("dbms.security.auth_enabled", "true")
                    .withConfig("org.neo4j.server.webserver.port", String.valueOf(neoPort))
                    .withConfig("dbms.security.auth_store.location", authStore.toAbsolutePath().toString())
                    .newServer();

            initialise(controls);

        } catch (Exception e) {
            throw new RuntimeException("Error starting in-process server",e);
        }
    }

    private static void initialise(ServerControls controls) throws Exception {

        Field field = InProcessServerControls.class.getDeclaredField( "server" );
        field.setAccessible( true );
        neoServer = (AbstractNeoServer) field.get(controls);
    }

    @Test
    public void testUnauthorizedSession() throws Exception {
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
    private boolean isRunningWithNeo4j2Dot2OrLater() throws Exception{
        Class<?> versionClass = null;
        BigDecimal version = new BigDecimal(2.1);
        try {
            versionClass = Class.forName("org.neo4j.kernel.Version");
            Method kernelVersion23x = versionClass.getDeclaredMethod("getKernelVersion", null);
            version = new BigDecimal(((String)kernelVersion23x.invoke(null)).substring(0,3));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            try {
                Method kernelVersion22x = versionClass.getDeclaredMethod("getKernelRevision", null);
                version = new BigDecimal(((String)kernelVersion22x.invoke(null)).substring(0,3));
            } catch (NoSuchMethodException e1) {
                throw new RuntimeException("Unable to find a method to get Neo4js kernel version");
            }

        }
        return version.compareTo(new BigDecimal("2.1")) > 0;
    }

    @Test
    public void testAuthorizedSession() throws Exception {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        init(AUTH, "org.neo4j.ogm.domain.bike");

        try ( Transaction ignored = session.beginTransaction() ) {
            session.loadAll(Bike.class);
        } catch (ResultProcessingException rpe) {
            fail("'" + rpe.getCause().getLocalizedMessage() + "' was not expected here");
        }

    }

    /**
     * @see issue #35
     */
    @Test
    public void testAuthorizedSessionWithSuppliedCredentials() throws Exception {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        initWithSuppliedCredentials("neo4j", "password", "org.neo4j.ogm.domain.bike");

        try ( Transaction ignored = session.beginTransaction() ) {
            session.loadAll(Bike.class);
        } catch (ResultProcessingException rpe) {
            fail("'" + rpe.getCause().getLocalizedMessage() + "' was not expected here");
        }

    }

    /**
     * @see issue #35
     */
    @Test
    public void testUnauthorizedSessionWithSuppliedCredentials() throws Exception {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        initWithSuppliedCredentials("neo4j", "incorrectPassword", "org.neo4j.ogm.domain.bike");

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

    /**
     * @see issue #35
     */
    @Test
    public void testAuthorizedSessionWithURI() throws Exception {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        initWithEmbeddedCredentials("http://neo4j:password@" + neoServer.baseUri().getHost() + ":" + neoServer.baseUri().getPort(), "org.neo4j.ogm.domain.bike");

        try ( Transaction ignored = session.beginTransaction() ) {
            session.loadAll(Bike.class);
        } catch (ResultProcessingException rpe) {
            fail("'" + rpe.getCause().getLocalizedMessage() + "' was not expected here");
        }

    }

    /**
     * @see issue #35
     */
    @Test
    public void testUnauthorizedSessionWithURI() throws Exception {
        assumeTrue(isRunningWithNeo4j2Dot2OrLater());

        initWithEmbeddedCredentials(neoServer.baseUri().toString(), "org.neo4j.ogm.domain.bike");

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

    private void initWithSuppliedCredentials(String username, String password, String... packages) {
        System.getProperties().remove("username");
        System.getProperties().remove("password");

        session = new SessionFactory(packages).openSession(neoServer.baseUri().toString(),username,password);
    }

    private void initWithEmbeddedCredentials(String url, String... packages) {
        System.getProperties().remove("username");
        System.getProperties().remove("password");

        session = new SessionFactory(packages).openSession(url);
    }
}
