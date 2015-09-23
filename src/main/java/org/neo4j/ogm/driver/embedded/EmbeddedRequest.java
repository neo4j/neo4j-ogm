package org.neo4j.ogm.driver.embedded;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.cypher.query.GraphRowModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQueryWithStatistics;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.session.request.Request;
import org.neo4j.ogm.session.response.GraphModelResponse;
import org.neo4j.ogm.session.response.Response;
import org.neo4j.ogm.session.response.RowModelResponse;
import org.neo4j.ogm.session.response.RowStatisticsResponse;
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
    public Response<GraphModel> execute(GraphModelQuery query) {
        return new GraphModelResponse(execute((ParameterisedStatement) query), mapper);
    }

    @Override
    public Response<RowModel> execute(RowModelQuery query) {
        return new RowModelResponse(execute((ParameterisedStatement) query), mapper);
    }

    @Override
    public Response<GraphRowModel> execute(GraphRowModelQuery query) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response<RowStatisticsModel> execute(RowModelQueryWithStatistics query) {
        Response<String> response = execute((ParameterisedStatement) query);
        response.expect(Response.ResponseRecord.STATS);
        return new RowStatisticsResponse(response, mapper);
    }

    private Response<String> execute(ParameterisedStatement statement) {

        try {
            String cypher = statement.getStatement();
            String params = mapper.writeValueAsString(statement.getParameters());
            TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};
            HashMap<String, Object> parameterMap = mapper.readValue(params.getBytes(), typeRef);
            return new EmbeddedResponse(graphDatabaseService.execute(cypher, parameterMap));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
