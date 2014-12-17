package org.neo4j.ogm.session.request;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.neo4j.ogm.session.response.JsonResponse;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRequest implements Neo4jRequest<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequest.class);

    private final CloseableHttpClient httpClient;

    public DefaultRequest(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Neo4jResponse<String> execute(String url, String cypherQuery) {

        try {
            HttpPost request = new HttpPost(url);
            HttpEntity entity = new StringEntity(cypherQuery);

            request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));
            request.setEntity(entity);

            LOGGER.debug("executing request: " + cypherQuery);
            HttpResponse response = httpClient.execute(request);

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

            LOGGER.debug("response is OK, creating response handler");
            return new JsonResponse(responseEntity.getContent());


        }
        catch (Exception e) {
            throw new ResultProcessingException("Failed to execute request: ", e);
        }
    }
}
