package org.neo4j.ogm.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.mapper.cypher.CypherStatements;

import java.io.IOException;

public class GraphModelRequestHandler implements Neo4jRequestHandler<GraphModel> {

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GraphModelRequestHandler(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Neo4jResponseHandler<GraphModel> execute(String url, String statement) {

        String cypherQuery = new CypherStatements().add(statement).toString();

        try {
            HttpPost request = new HttpPost(url);
            HttpEntity entity = new StringEntity(cypherQuery);

            request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));
            request.setEntity(entity);

            return new GraphModelResponseHandler((GraphModelResult[]) httpClient.execute(request, rh));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    ResponseHandler rh = new ResponseHandler() {

        @Override
        public GraphModelResult[] handleResponse(final HttpResponse response) throws IOException {

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

            String responseString = EntityUtils.toString(entity);
            CypherGraphModelResponse graphModelResponse = objectMapper.readValue(responseString, CypherGraphModelResponse.class);

            // TODO: check for errors in the response
            return graphModelResponse.getResults()[0].getData();
        }
    };

}
