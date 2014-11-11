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
import org.neo4j.ogm.mapper.cypher.CypherStatements;

import java.io.IOException;

public class RowModelRequestHandler implements Neo4jRequestHandler<RowModel> {

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RowModelRequestHandler(CloseableHttpClient client, ObjectMapper mapper) {
        this.httpClient = client;
        this.objectMapper = mapper;
    }

    @Override
    public Neo4jResponseHandler<RowModel> execute(String url, String... statements) {
        CypherStatements cypherStatements = new CypherStatements();
        for (String statement : statements) {
            cypherStatements.add(statement);
        }
        String cypherQuery = cypherStatements.toString();
        try {
            HttpPost request = new HttpPost(url);
            HttpEntity entity = new StringEntity(cypherQuery);

            request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));
            request.setEntity(entity);

            return new RowModelResponseHandler((RowModelResult[]) httpClient.execute(request, rh));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    ResponseHandler rh = new ResponseHandler() {

        @Override
        public RowModelResult[] handleResponse(final HttpResponse response) throws IOException {

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

            RowModelResponse rowModelResponse = objectMapper.readValue(responseString, RowModelResponse.class);
            if (rowModelResponse.getErrors().length > 0) {
                for (Object error : rowModelResponse.getErrors()) {
                    System.out.println(error);
                }
                throw new RuntimeException("Error executing statements");
            }


            if (rowModelResponse.getResults().length > 0) {
                return rowModelResponse.getResults()[0].getData();
            } else {
                return null;
            }

        }
    };

}
