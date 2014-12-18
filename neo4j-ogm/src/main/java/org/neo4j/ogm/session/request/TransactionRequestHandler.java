package org.neo4j.ogm.session.request;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.neo4j.ogm.session.transaction.LongTransaction;
import org.neo4j.ogm.session.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionRequestHandler {

    private final Logger logger = LoggerFactory.getLogger(TransactionRequestHandler.class);
    private final CloseableHttpClient httpClient;
    private final String url;

    public TransactionRequestHandler(CloseableHttpClient httpClient, String server) {
        this.url = transactionRequestEndpoint(server);
        this.httpClient = httpClient;
    }

    public Transaction openTransaction(MappingContext mappingContext) {
        String transactionEndpoint = newTransactionEndpointUrl();
        logger.info("creating new transaction with endpoint " + transactionEndpoint);
        return new LongTransaction(mappingContext, transactionEndpoint, this);
    }

    public void rollback(Transaction tx) {
        String url = tx.url();
        logger.info("DELETE " + url);
        HttpDelete request = new HttpDelete(url);
        executeRequest(request);
    }

    public void commit(Transaction tx) {
        String url = tx.url() + "/commit";
        logger.info("POST " + url);
        HttpPost request = new HttpPost(url);
        request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
        executeRequest(request);
    }

    private HttpResponse executeRequest(HttpRequestBase request) {
        try {

            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));

            HttpResponse response = httpClient.execute(request);
            StatusLine statusLine = response.getStatusLine();

            logger.info("Status code: " + statusLine.getStatusCode());
            if (statusLine.getStatusCode() >= 300) {
                throw new HttpResponseException(
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }
            // we're not interested in the content, but we must always close the content stream
            try {
                response.getEntity().getContent().close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return response;
        }
        catch (Exception e) {
            throw new ResultProcessingException("Failed to execute request: ", e);
        }
    }

    private String newTransactionEndpointUrl() {
        logger.info("POST " + url);
        HttpPost request = new HttpPost(url);
        request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
        HttpResponse response = executeRequest(request);
        Header location = response.getHeaders("Location")[0];
        return location.getValue();
    }

    private String transactionRequestEndpoint(String server) {
        if (server == null) {
            return server;
        }
        String url = server;

        if (!server.endsWith("/")) {
            url += "/";
        }
        return url + "db/data/transaction";
    }
}
