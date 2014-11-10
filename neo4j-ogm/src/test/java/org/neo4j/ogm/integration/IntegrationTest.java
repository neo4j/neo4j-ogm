package org.neo4j.ogm.integration;

import org.junit.After;
import org.junit.Before;
import org.neo4j.ogm.session.Session;
import org.neo4j.server.NeoServer;
import org.neo4j.server.helpers.CommunityServerBuilder;

import java.io.IOException;
import java.net.ServerSocket;

public class IntegrationTest     {

    private NeoServer neoServer;

    protected Session session;
    protected int neoPort;

    @Before
    public void setUp() throws IOException {
        neoPort = getAvailablePort();
        neoServer = CommunityServerBuilder.server().onPort(neoPort).build();
        neoServer.start();
    }

    @After
    public void tearDown() {
        neoServer.stop();
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
