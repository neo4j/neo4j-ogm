package org.neo4j.ogm.integration;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.neo4j.ogm.session.request.TransactionRequestHandler;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TransactionRequestHandlerTest extends IntegrationTest {

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Test
    public void testObtainNewRestTransactionEndpoint() {

        TransactionRequestHandler txRequestHandler = new TransactionRequestHandler(httpClient, "http://localhost:" + neoPort);

        String txEndpoint = txRequestHandler.openTransaction();
        String txEndpointRoot = "http://localhost:" + neoPort + "/db/data/transaction/";
        String txId = txEndpoint.substring(txEndpointRoot.length());

        assertTrue(txEndpoint.startsWith(txEndpointRoot));
        try {
            int id = Integer.parseInt(txId);
            assertTrue (id > 0);
        } catch (Exception e) {
            fail("Should have been a number: " + txId);
        }

    }



}
