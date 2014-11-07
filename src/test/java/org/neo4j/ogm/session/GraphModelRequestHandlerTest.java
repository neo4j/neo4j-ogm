package org.neo4j.ogm.session;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.graphaware.graphmodel.neo4j.NodeModel;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;

public class GraphModelRequestHandlerTest {

    @Test
    public void testExecute() throws Exception {

        // this takes several seconds to initialise. we should have a singleton in our test suite.
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String url = "http://localhost:7474/db/data/transaction/commit";

        Neo4jRequestHandler<GraphModel> requestHandler = new GraphModelRequestHandler(httpClient);
        Neo4jResponseHandler<GraphModel> responseHandler = requestHandler.execute(url, new CypherQuery().findOne(0L));

        GraphModel graphModel;
        while ((graphModel=responseHandler.next()) != null) {
            for (NodeModel nodeModel : graphModel.getNodes()) {
                assertNotNull(nodeModel.getId());
            }

        }

        httpClient.close();

    }
}
