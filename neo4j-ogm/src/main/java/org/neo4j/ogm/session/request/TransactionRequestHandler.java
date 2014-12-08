package org.neo4j.ogm.session.request;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.neo4j.ogm.session.result.ResultProcessingException;

public class TransactionRequestHandler {

    private final CloseableHttpClient httpClient;
    private final String url;

    public TransactionRequestHandler(CloseableHttpClient httpClient, String server) {
        this.url = transactionEndpoint(server);
        this.httpClient = httpClient;
    }

    public String openTransaction() {
        try {
            HttpPost request = new HttpPost(url);
            HttpEntity entity = new StringEntity("");

            request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() >= 300) {
                throw new HttpResponseException(
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }

            Header location = response.getHeaders("Location")[0];
            return location.getValue();

        }
        catch (Exception e) {
            throw new ResultProcessingException("Failed to execute request: ", e);
        }
    }

    public void rollback() {

    }

    public void commit() {

    }

    private String transactionEndpoint(String server) {
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
