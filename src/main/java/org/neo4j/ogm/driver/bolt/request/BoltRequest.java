package org.neo4j.ogm.driver.bolt.request;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.ogm.cypher.query.GraphModelRequest;
import org.neo4j.ogm.cypher.query.GraphRowModelRequest;
import org.neo4j.ogm.cypher.query.RowModelRequest;
import org.neo4j.ogm.cypher.query.RowModelStatisticsRequest;
import org.neo4j.ogm.cypher.statement.Statement;
import org.neo4j.ogm.driver.EmptyResponse;
import org.neo4j.ogm.driver.bolt.response.GraphModelResponse;
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

    private final Session transport;

    public BoltRequest(Session transport) {
        this.transport = transport;
    }

    @Override
    public Response<GraphModel> execute(GraphModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new GraphModelResponse(executeRequest(request));
    }

    @Override

    public Response<RowModel> execute(RowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return null;//new RowModelResponse(executeRequest(request));
    }

    @Override
    public Response<GraphRowModel> execute(GraphRowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return null;//new GraphRowModelResponse(executeRequest(request));
    }

    @Override
    public Response<RowStatisticsModel> execute(RowModelStatisticsRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return null;//new RowStatisticsModelResponse(executeRequest(request));
    }

    private Result executeRequest(Statement request) {
        return transport.run(request.getStatement(), toValueMap(request.getParameters()));
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
