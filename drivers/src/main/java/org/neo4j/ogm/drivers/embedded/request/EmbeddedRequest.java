package org.neo4j.ogm.drivers.embedded.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.model.RowStatisticsModel;
import org.neo4j.ogm.request.*;
import org.neo4j.ogm.response.EmptyResponse;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.drivers.embedded.response.GraphModelResponse;
import org.neo4j.ogm.drivers.embedded.response.GraphRowModelResponse;
import org.neo4j.ogm.drivers.embedded.response.RowModelResponse;
import org.neo4j.ogm.drivers.embedded.response.RowStatisticsModelResponse;

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
    public Response<GraphRowListModel> execute(GraphRowListModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new GraphRowModelResponse(startTransaction(), executeRequest(request));
    }

    @Override
    public Response<RowStatisticsModel> execute(RowStatisticsModelRequest request) {
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
