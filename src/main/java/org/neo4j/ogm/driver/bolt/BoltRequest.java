package org.neo4j.ogm.driver.bolt;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.ogm.cypher.query.GraphModelRequest;
import org.neo4j.ogm.cypher.query.GraphRowModelRequest;
import org.neo4j.ogm.cypher.query.RowModelRequest;
import org.neo4j.ogm.cypher.query.RowModelStatisticsRequest;
import org.neo4j.ogm.cypher.statement.Statement;
import org.neo4j.ogm.session.request.Request;
import org.neo4j.ogm.session.response.Response;
import org.neo4j.ogm.session.response.model.GraphModel;
import org.neo4j.ogm.session.response.model.GraphRowModel;
import org.neo4j.ogm.session.response.model.RowModel;
import org.neo4j.ogm.session.response.model.RowStatisticsModel;

import java.util.Map;

/**
 * @author vince
 */
public class BoltRequest implements Request {

    private final Session session;

    public BoltRequest(Session session) {
        this.session = session;
    }

    @Override
    public Response<GraphModel> execute(GraphModelRequest query) {

        Result result = executeRequest(query);

        Response<GraphModel> response = new GraphModelResponse(result);

        return response;
    }

    @Override

    public Response<RowModel> execute(RowModelRequest query) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Response<GraphRowModel> execute(GraphRowModelRequest query) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Response<RowStatisticsModel> execute(RowModelStatisticsRequest query) {
        throw new RuntimeException("not implemented");
    }

    private Result executeRequest(Statement request) {
        return session.run(request.getStatement(), toValueMap(request.getParameters()));
    }

    private Map<String, Value> toValueMap(Map<String, Object> params) {

        if (params != null && !params.isEmpty()) {
            Object[] kvs = new Object[(params.size() -1) * 2];
            int i = 0;
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                kvs[i++] = entry.getKey();
                kvs[i++] = entry.getValue();
            }
            return Values.parameters(kvs);
        }
        return null;

    }
}
