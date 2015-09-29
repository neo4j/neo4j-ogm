package org.neo4j.ogm.driver.embedded.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.ogm.cypher.query.GraphModelRequest;
import org.neo4j.ogm.cypher.query.GraphRowModelRequest;
import org.neo4j.ogm.cypher.query.RowModelRequest;
import org.neo4j.ogm.cypher.query.RowModelStatisticsRequest;
import org.neo4j.ogm.cypher.statement.Statement;
import org.neo4j.ogm.driver.EmptyResponse;
import org.neo4j.ogm.driver.embedded.response.GraphModelResponse;
import org.neo4j.ogm.driver.embedded.response.GraphRowModelResponse;
import org.neo4j.ogm.driver.embedded.response.RowModelResponse;
import org.neo4j.ogm.driver.embedded.response.RowStatisticsModelResponse;
import org.neo4j.ogm.session.request.Request;
import org.neo4j.ogm.session.response.Response;
import org.neo4j.ogm.session.response.model.GraphModel;
import org.neo4j.ogm.session.response.model.GraphRowModel;
import org.neo4j.ogm.session.response.model.RowModel;
import org.neo4j.ogm.session.response.model.RowStatisticsModel;

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
