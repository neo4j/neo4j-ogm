/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package neo4j.ogm.drivers.bolt.request;


import java.util.Map;

import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Value;
import org.neo4j.driver.Values;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.DefaultRequest;
import org.neo4j.ogm.request.GraphModelRequest;
import org.neo4j.ogm.request.GraphRowListModelRequest;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.request.RestModelRequest;
import org.neo4j.ogm.request.RowModelRequest;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.response.EmptyResponse;
import org.neo4j.ogm.response.Response;

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
        return null;//return new GraphModelResponse(executeRequest(request));
    }

    @Override

    public Response<RowModel> execute(RowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return null;//new RowModelResponse(executeRequest(request));
    }

    @Override
    public Response<RowModel> execute(DefaultRequest query) {
        return null; //TODO
    }

    @Override
    public Response<GraphRowListModel> execute(GraphRowListModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return null;//new GraphRowModelResponse(executeRequest(request));
    }


    @Override
    public Response<RestModel> execute(RestModelRequest query) {
        return null;
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
