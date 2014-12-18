package org.neo4j.ogm.integration;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;
import org.neo4j.ogm.session.request.TransactionRequestHandler;
import org.neo4j.ogm.session.transaction.Transaction;

import static org.junit.Assert.assertEquals;

public class TransactionRequestHandlerTest extends IntegrationTest {

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Test
    public void testCreateLongTransaction() {

        TransactionRequestHandler txRequestHandler = new TransactionRequestHandler(httpClient, "http://localhost:" + neoPort);
        try (Transaction tx = txRequestHandler.openTransaction(null)) {
            assertEquals(Transaction.Status.OPEN, tx.status());
        }
    }

    @Test
    public void testCreateConcurrentTransactions() {

        TransactionRequestHandler txRequestHandler = new TransactionRequestHandler(httpClient, "http://localhost:" + neoPort);

        // note that the try-with-resources implies these transactions are nested, but they are in fact independent
        try (Transaction tx1 = txRequestHandler.openTransaction(null)) {
            try (Transaction tx2 = txRequestHandler.openTransaction(null)) {
                assertEquals(Transaction.Status.OPEN, tx1.status());
                assertEquals(Transaction.Status.OPEN, tx2.status());
            }
        }
    }

}
