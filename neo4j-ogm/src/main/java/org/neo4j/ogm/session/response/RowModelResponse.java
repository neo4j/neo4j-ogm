package org.neo4j.ogm.session.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.session.result.RowModel;
import org.neo4j.ogm.session.result.RowModelResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RowModelResponse implements Neo4jResponse<RowModel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RowModelResponse.class);

    private final ObjectMapper objectMapper;
    private final Neo4jResponse<String> response;

    public RowModelResponse(Neo4jResponse<String> response, ObjectMapper mapper) {
        this.response = response;
        this.objectMapper = mapper;
        initialiseScan("row");
    }

    @Override
    public RowModel next() {
        String json = response.next();
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
        response.close();
    }

    @Override
    public void initialiseScan(String token) {
        response.initialiseScan(token);
    }

    @Override
    public String[] columns() {
        return response.columns();
    }

    @Override
    public int rowId() {
        return response.rowId();
    }
}
