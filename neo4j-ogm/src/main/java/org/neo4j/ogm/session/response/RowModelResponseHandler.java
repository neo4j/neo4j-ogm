package org.neo4j.ogm.session.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.session.result.RowModel;
import org.neo4j.ogm.session.result.RowModelResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowModelResponseHandler implements Neo4jResponseHandler<RowModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RowModelResponseHandler.class);

    private final ObjectMapper objectMapper;
    private final Neo4jResponseHandler<String> responseHandler;

    public RowModelResponseHandler(Neo4jResponseHandler<String> responseHandler, ObjectMapper mapper) {
        this.responseHandler = responseHandler;
        this.objectMapper = mapper;
        initialiseScan("row");
    }

    @Override
    public RowModel next() {
        String json = responseHandler.next();
        if (json != null) {
            try {
                return new RowModel(objectMapper.readValue(json, RowModelResult.class).getRow());
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
    public void initialiseScan(String token) {
        responseHandler.initialiseScan(token);
    }

    @Override
    public String[] columns() {
        return responseHandler.columns();
    }
}
