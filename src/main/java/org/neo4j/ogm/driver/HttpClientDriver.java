package org.neo4j.ogm.driver;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.neo4j.ogm.authentication.HttpRequestAuthorization;
import org.neo4j.ogm.authentication.Neo4jCredentials;
import org.neo4j.ogm.session.response.JsonResponse;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.result.ErrorsException;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.neo4j.ogm.session.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public class HttpClientDriver implements Driver<String> {


    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    private final Logger logger = LoggerFactory.getLogger(HttpClientDriver.class);

    private Neo4jCredentials credentials = null;

    @Override
    public void authorize(Neo4jCredentials credentials) {
        this.credentials = credentials;
    }

    @Override  // not needed
    public Object execute(Object request) throws Exception {
        return httpClient.execute((HttpRequestBase) request);
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (Exception e) {
            logger.warn("Unexpected Exception when closing http client transport: ", e);
        }
    }

    @Override
    public void rollback(Transaction tx) {
        String url = tx.url();
        logger.debug("DELETE " + url);
        HttpDelete request = new HttpDelete(url);
        executeRequest(request);
    }

    @Override
    public void commit(Transaction tx) {
        String url = tx.url() + "/commit";
        logger.debug("POST " + url);
        HttpPost request = new HttpPost(url);
        request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
        executeRequest(request);
    }

    @Override
    public String newTransactionUrl(String server) {
        logger.debug("POST " + server);
        HttpPost request = new HttpPost(server);
        request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
        HttpResponse response = executeRequest(request);
        Header location = response.getHeaders("Location")[0];
        return location.getValue();
    }

    @Override
    public Neo4jResponse<String> execute(String url, String cypherQuery) {

        JsonResponse jsonResponse = null;

        try {

            logger.debug("POST " + url + ", request: " + cypherQuery);

            HttpPost request = new HttpPost(url);
            HttpEntity entity = new StringEntity(cypherQuery,"UTF-8");

            request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));

            // http://tools.ietf.org/html/rfc7231#section-5.5.3
            request.setHeader(new BasicHeader("User-Agent", "neo4j-ogm.java/1.0"));

            HttpRequestAuthorization.authorize(request, credentials);

            request.setEntity(entity);

            CloseableHttpResponse response = httpClient.execute(request);

            StatusLine statusLine = response.getStatusLine();
            HttpEntity responseEntity = response.getEntity();

            if (statusLine.getStatusCode() >= 300) {
                throw new HttpResponseException(
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }
            if (responseEntity == null) {
                throw new ClientProtocolException("Response contains no content");
            }

            logger.debug("Response is OK, creating response handler");
            jsonResponse = new JsonResponse(response);
            return jsonResponse;

        }
        // the primary exception handler, will ensure all resources are properly closed
        catch (Exception e) {
            logger.warn("Caught response exception: " + e.getLocalizedMessage());
            if (jsonResponse != null) {
                jsonResponse.close();
            }
            throw new ResultProcessingException("Failed to execute request: " + cypherQuery, e);
        }
    }

    private CloseableHttpResponse executeRequest(HttpRequestBase request) {

        //assert(credentials != null);

        try {

            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));


            HttpRequestAuthorization.authorize(request, credentials);

            CloseableHttpResponse response = httpClient.execute(request);
            StatusLine statusLine = response.getStatusLine();

            logger.debug("Status code: " + statusLine.getStatusCode());
            if (statusLine.getStatusCode() >= 300) {
                throw new HttpResponseException(
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }
            // close the content stream/release the connection
            HttpEntity responseEntity = response.getEntity();

            if (responseEntity != null) {
                String responseText = EntityUtils.toString(responseEntity);
                logger.debug(responseText);
                EntityUtils.consume(responseEntity);
                if (responseText.contains("\"errors\":[{") || responseText.contains("\"errors\": [{")) {
                    throw new ErrorsException(responseText);
                }
            }
            return response;
        }

        catch (Exception e) {
            throw new ResultProcessingException("Failed to execute request: ", e);
        }

        finally {
            request.releaseConnection();
        }
    }

}
