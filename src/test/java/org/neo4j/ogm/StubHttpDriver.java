/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */

package org.neo4j.ogm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.cypher.query.GraphRowModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQueryWithStatistics;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.request.Request;
import org.neo4j.ogm.session.response.model.GraphModel;
import org.neo4j.ogm.session.response.GraphModelResponse;
import org.neo4j.ogm.session.response.Response;
import org.neo4j.ogm.session.response.model.GraphRowModel;
import org.neo4j.ogm.session.response.model.RowModel;
import org.neo4j.ogm.session.response.model.RowStatisticsModel;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;

/**
 * @author Vince Bickers
 */
public abstract class StubHttpDriver implements Driver {

    private ObjectMapper mapper = new ObjectMapper();

    protected abstract String[] getResponse();

    static class StubResponse implements Response<String> {

        private final String[] jsonModel;
        private int count = 0;

        public StubResponse(String[] jsonModel) {
            this.jsonModel = jsonModel;
        }

        public String next()  {
            if (count < jsonModel.length) {
                String json = jsonModel[count];
                count++;
                return json;
            }
            return null;
        }

        @Override
        public void close() {
            // nothing to do.
        }

        @Override
        public void expect(ResponseRecord record) {
            // nothing to do
        }

        @Override
        public String[] columns() {
            return new String[0];
        }

        @Override
        public int rowId() {
            return count-1;
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(DriverConfig config) {

    }

    @Override
    public Transaction openTransaction(MappingContext context, TransactionManager tx, boolean autoCommit) {
        return null;
    }

    @Override
    public Object getConfig(String key) {
        return null;
    }

    @Override
    public Request requestHandler() {

        return new Request() {
            @Override
            public Response<GraphModel> execute(GraphModelQuery qry) {
                return new GraphModelResponse(new StubResponse(getResponse()), mapper);
            }

            @Override
            public Response<RowModel> execute(RowModelQuery query) {
                return null;
            }

            @Override
            public Response<GraphRowModel> execute(GraphRowModelQuery query) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public Response<String> execute(ParameterisedStatement statement) {
                return new StubResponse(getResponse());
            }
            @Override
            public Response<RowStatisticsModel> execute(RowModelQueryWithStatistics query) {
                return null;  //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

}
