package org.neo4j.ogm.drivers.bolt.request;


import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.model.GraphRows;
import org.neo4j.ogm.api.model.Row;
import org.neo4j.ogm.api.model.RowStatistics;
import org.neo4j.ogm.api.request.*;
import org.neo4j.ogm.api.response.EmptyResponse;
import org.neo4j.ogm.api.response.Response;

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
    public Response<Graph> execute(GraphModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return null;//return new GraphModelResponse(executeRequest(request));
    }

    @Override

    public Response<Row> execute(RowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return null;//new RowModelResponse(executeRequest(request));
    }

    @Override
    public Response<GraphRows> execute(GraphRowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return null;//new GraphRowModelResponse(executeRequest(request));
    }

    @Override
    public Response<RowStatistics> execute(RowModelStatisticsRequest request) {
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
