package org.neo4j.ogm.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.neo4j.ogm.session.querystrategy.DepthOneStrategy;
import org.neo4j.ogm.session.request.DefaultRequestHandler;
import org.neo4j.ogm.session.response.JsonResponseHandler;
import org.neo4j.ogm.session.result.GraphModelResult;
import org.neo4j.ogm.session.result.SessionException;

import static org.junit.Assert.assertTrue;

public class RemoteStreamingPerformanceTest {

    // this takes several seconds to initialise. we should have a singleton in our test suite.
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DefaultRequestHandler requestHandler = new DefaultRequestHandler(httpClient);

    private String url = "http://localhost:7474/db/data/transaction/commit";


    @Test
    public void testFindAllLengthOnePaths() throws Exception {

        try {
            JsonResponseHandler responseHandler = (JsonResponseHandler) requestHandler.execute(url, new DepthOneStrategy().findAll());
            // todo: the request handler should know whether its getting a graph or row response model
            responseHandler.setScanToken("graph");

            String json;
            int count = 0;
            long elapsed = -System.currentTimeMillis();

            while ((json=responseHandler.next()) != null) {
                count++;
                try {
                    objectMapper.readValue(json, GraphModelResult.class);
                } catch (Exception e) {
                    System.out.println("failed to parse: " + json + " at row " + count);
                    throw new RuntimeException(e);
                }
                if (count % 10000 == 0) {
                    System.out.println(count);
                }
            }

            elapsed += System.currentTimeMillis();
            System.out.format("loaded " + count + " distinct paths '(m)-->(n)' from the graph in %d milliseconds\n", elapsed);

            assertTrue(2500 < (count * 1000 / elapsed));


            responseHandler.close();
        } catch (SessionException se) {
            System.out.println("Tests failed to run. Probable cause: " +  se.getCause().getLocalizedMessage());
        }
        finally {
            httpClient.close();
        }
    }
}
