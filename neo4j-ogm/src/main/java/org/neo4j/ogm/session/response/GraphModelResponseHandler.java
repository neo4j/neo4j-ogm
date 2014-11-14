package org.neo4j.ogm.session.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.graphaware.graphmodel.neo4j.GraphModel;
import org.neo4j.ogm.session.result.GraphModelResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphModelResponseHandler implements Neo4jResponseHandler<GraphModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphModelResponseHandler.class);

    private final ObjectMapper objectMapper;
    private final Neo4jResponseHandler<String> responseHandler;

    public GraphModelResponseHandler(Neo4jResponseHandler<String> responseHandler, ObjectMapper mapper) {
        this.responseHandler = responseHandler;
        this.objectMapper = mapper;
        setScanToken("graph");
    }

    @Override
    public GraphModel next() {

        String json = responseHandler.next();

        if (json != null) {
            try {
                return objectMapper.readValue(json, GraphModelResult.class).getGraph();
            } catch (Exception e) {
                LOGGER.error("failed to parse: " + json);
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    @Override
    public void close() {
        responseHandler.close();
    }

    @Override
    public void setScanToken(String token) {
        responseHandler.setScanToken(token);
    }

}
