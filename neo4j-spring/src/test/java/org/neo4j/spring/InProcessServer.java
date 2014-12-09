package org.neo4j.spring;

import org.neo4j.server.NeoServer;
import org.neo4j.server.helpers.CommunityServerBuilder;
import org.springframework.data.neo4j.server.Neo4jServer;

import java.io.IOException;
import java.net.ServerSocket;

public class InProcessServer implements Neo4jServer {

    private final NeoServer neoServer;
    protected int neoPort;

    public InProcessServer()  {
        neoPort = getAvailablePort();
        try {
            neoServer = CommunityServerBuilder.server().onPort(neoPort).build();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    neoServer.stop();
                }
            });
            neoServer.start();
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
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