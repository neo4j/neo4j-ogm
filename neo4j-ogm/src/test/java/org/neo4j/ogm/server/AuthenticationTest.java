package org.neo4j.ogm.server;

import org.apache.http.client.HttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.Test;
import org.neo4j.ogm.integration.LocalhostServerTest;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.neo4j.ogm.session.transaction.Transaction;

import java.io.IOException;

import static org.junit.Assert.*;

public class AuthenticationTest extends LocalhostServerTest {

    private Session session;

    private boolean AUTH = true;
    private boolean NO_AUTH = false;

    @Test
    public void testUnauthorizedSession() {

        init(NO_AUTH, "org.neo4j.ogm.domain.bike");

        try {
            Transaction tx = session.beginTransaction();
            session.purgeDatabase();
            tx.close();
            logVersion();
        } catch (ResultProcessingException rpe) {
            Throwable cause = rpe.getCause();
            if (cause instanceof HttpHostConnectException) {
                logNotRunning();
            } else {
                assertTrue(cause instanceof HttpResponseException);
                assertEquals("Unauthorized", cause.getMessage());
            }
        }

    }

    @Test
    public void testAuthorizedSession() {

        init(AUTH, "org.neo4j.ogm.domain.bike");

        try {
            Transaction tx = session.beginTransaction();
            session.purgeDatabase();
            tx.close();
        } catch (ResultProcessingException rpe) {
            fail("'" + rpe.getCause().getLocalizedMessage() + "' was not expected here");
        }

    }

    private void init(boolean auth, String... packages){

        if (auth) {
            System.setProperty("username", "neo4j");
            System.setProperty("password", "password");
        } else {
            System.getProperties().remove("username");
            System.getProperties().remove("password");
        }

        try {
            session = super.session(packages);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void logVersion() {
        System.out.println("A non-authenticating version of Neo4j is running. Please start Neo4j 2.2.0 or later to run these tests");
    }

    private void logNotRunning() {
        System.out.println("Please start Neo4j 2.2.0 or later to run these tests");
    }

}
