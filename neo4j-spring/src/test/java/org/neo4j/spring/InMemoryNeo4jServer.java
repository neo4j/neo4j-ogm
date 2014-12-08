package org.neo4j.spring;

import org.neo4j.server.NeoServer;
import org.neo4j.server.helpers.CommunityServerBuilder;

import java.io.IOException;
import java.net.ServerSocket;

public class InMemoryNeo4jServer implements Neo4jServer {

    private final NeoServer neoServer;
    protected int neoPort;

    public InMemoryNeo4jServer() throws IOException {
        neoPort = getAvailablePort();
        neoServer = CommunityServerBuilder.server().onPort(neoPort).build();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                neoServer.stop();
            }
        });

        neoServer.start();
    }

    public String url() {
        return neoServer.baseUri().toString();
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