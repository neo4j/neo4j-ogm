package org.neo4j.ogm.session;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.mapper.cypher.CypherStatements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class GraphModelRequestHandler implements Neo4jRequestHandler<GraphModel> {

    private final HttpClient httpClient;

    public GraphModelRequestHandler(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Neo4jResponseHandler<GraphModel> execute(String url, String request) {

        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type","application/json");
            httpPost.setHeader("accept", "application/json");
            httpPost.setEntity(new StringEntity(new CypherStatements().add(request).toString()));

            return new GraphModelResponseHandler((BufferedReader) httpClient.execute(httpPost, rh));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ResponseHandler rh = new ResponseHandler() {

        @Override
        public BufferedReader handleResponse(final HttpResponse response) throws IOException {

            StatusLine statusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();

            if (statusLine.getStatusCode() >= 300) {
                throw new HttpResponseException(
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }
            if (entity == null) {
                throw new ClientProtocolException("Response contains no content");
            }

            ContentType contentType = ContentType.getOrDefault(entity);
            Charset charset = contentType.getCharset();

            return new BufferedReader(new InputStreamReader(entity.getContent(), charset));

        }
    };

}
