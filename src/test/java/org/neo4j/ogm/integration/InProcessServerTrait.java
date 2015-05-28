package org.neo4j.ogm.integration;

import org.junit.experimental.categories.Category;
import org.neo4j.ogm.testutil.TestUtils;
import org.neo4j.server.NeoServer;
import org.neo4j.server.helpers.CommunityServerBuilder;

import java.io.IOException;

/**
 * @author: Vince Bickers
 */
@Category(Integration.class)
public class InProcessServerTrait {

    private final NeoServer neoServer;
    protected int neoPort;

    public InProcessServerTrait()  {
        neoPort = TestUtils.getAvailablePort();
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
}
