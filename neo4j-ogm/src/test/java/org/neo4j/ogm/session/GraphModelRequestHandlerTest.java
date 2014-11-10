package org.neo4j.ogm.session;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        ObjectMapper objectMapper = new ObjectMapper();
        String url = "http://localhost:7474/db/data/transaction/commit";


        long elapsed = -System.currentTimeMillis();

        Neo4jRequestHandler<GraphModel> requestHandler = new GraphModelRequestHandler(httpClient, objectMapper);
        Neo4jResponseHandler<GraphModel> responseHandler = requestHandler.execute(url, new CypherQuery().findAll());
        GraphModel graphModel;
        int count = 0;
        while ((graphModel=responseHandler.next()) != null) {
            for (NodeModel nodeModel : graphModel.getNodes()) {
                assertNotNull(nodeModel.getId());
                count++;
            }
        }
        elapsed += System.currentTimeMillis();

        System.out.format("loaded " + count + " distinct paths '(m)-->(n)' from the graph in %d milliseconds\n", elapsed);

        httpClient.close();

    }
}
