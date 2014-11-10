package org.neo4j.ogm.integration;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.mapper.domain.bike.Bike;
import org.neo4j.ogm.session.DefaultSessionImpl;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.server.NeoServer;
import org.neo4j.server.helpers.CommunityServerBuilder;

import java.io.IOException;
import java.net.ServerSocket;

/**
 *  Temporary playground for the full cycle.
 */
public class EndToEndTest {

    private int neoPort = getAvailablePort();
    private NeoServer neoServer;
    private Session session;

    @Before
    public void setUp() throws IOException {
        neoServer = CommunityServerBuilder.server().onPort(neoPort).build();
        neoServer.start();

        session = new SessionFactory().openSession("http://localhost:" + neoPort);
    }

    @After
    public void tearDown() {
        neoServer.stop();
    }

    @Test
    public void canSaveModelToEmptyDatabase() {
        Bike bike = new Bike();

        //save bike,...
    }

    private static int getAvailablePort() {
        try {
            ServerSocket socket = new ServerSocket(0);
            try {
                return socket.getLocalPort();
            } finally {
                socket.close();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot find available port: " + e.getMessage(), e);
        }
    }
}
