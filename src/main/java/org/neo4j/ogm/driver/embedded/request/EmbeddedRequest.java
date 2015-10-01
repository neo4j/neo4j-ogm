package org.neo4j.ogm.driver.embedded.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.driver.api.request.*;
import org.neo4j.ogm.driver.api.response.Response;
import org.neo4j.ogm.driver.embedded.response.GraphModelResponse;
import org.neo4j.ogm.driver.embedded.response.GraphRowModelResponse;
import org.neo4j.ogm.driver.embedded.response.RowModelResponse;
import org.neo4j.ogm.driver.embedded.response.RowStatisticsModelResponse;
import org.neo4j.ogm.driver.impl.model.GraphModel;
import org.neo4j.ogm.driver.impl.model.GraphRowModel;
import org.neo4j.ogm.driver.impl.model.RowModel;
import org.neo4j.ogm.driver.impl.model.RowStatisticsModel;
import org.neo4j.ogm.driver.impl.response.EmptyResponse;

import java.util.HashMap;

/**
 * @author vince
 */
public class EmbeddedRequest implements Request {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final GraphDatabaseService graphDatabaseService;

    public EmbeddedRequest(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    @Override
    public Response<GraphModel> execute(GraphModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new GraphModelResponse(startTransaction(), executeRequest(request));
    }

    @Override
    public Response<RowModel> execute(RowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new RowModelResponse(startTransaction(), executeRequest(request));
    }

    @Override
    public Response<GraphRowModel> execute(GraphRowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new GraphRowModelResponse(startTransaction(), executeRequest(request));
    }

    @Override
    public Response<RowStatisticsModel> execute(RowModelStatisticsRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new RowStatisticsModelResponse(startTransaction(), executeRequest(request));
    }

    private Result executeRequest(Statement statement) {

        try {
            String cypher = statement.getStatement();
            String params = mapper.writeValueAsString(statement.getParameters());
            TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
            HashMap<String, Object> parameterMap = mapper.readValue(params.getBytes(), typeRef);
            return graphDatabaseService.execute(cypher, parameterMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Transaction startTransaction() {
        return graphDatabaseService.beginTx();
    }

}
