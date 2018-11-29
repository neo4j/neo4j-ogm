/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.drivers;

import org.neo4j.ogm.config.ObjectMapperFactory;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.exception.ResultProcessingException;
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
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultGraphRowListModel;
import org.neo4j.ogm.response.model.DefaultRowModel;
import org.neo4j.ogm.result.ResultGraphModel;
import org.neo4j.ogm.result.ResultRowModel;
import org.neo4j.ogm.transaction.Transaction;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Vince Bickers
 */
public abstract class StubHttpDriver extends AbstractConfigurableDriver {

    private final ObjectMapper mapper = ObjectMapperFactory.objectMapper();

    protected abstract String[] getResponse();

    protected String[] getResponse(String query) {
        return getResponse();
    }

    @Override
    public void close() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Transaction newTransaction(Transaction.Type type, Iterable<String> bookmarks) {
        throw new RuntimeException("not implemented");
    }

    static class Holder {
        final String[] json;

        private int count = 0;

        public Holder(String[] json) {
            this.json = json;
        }

        private String nextRecord() {
            if (count < json.length) {
                String r = json[count];
                count++;
                return r;
            }
            return null;
        }
    }

    @Override
    public Request request() {

        return new Request() {

            @Override
            public Response<GraphModel> execute(GraphModelRequest qry) {

                Holder holder = new Holder(getResponse(qry.getStatement()));
                return new Response<GraphModel>() {

                    @Override
                    public GraphModel next() {
                        String r = holder.nextRecord();
                        if (r != null) {
                            try {
                                return mapper.readValue(r, ResultGraphModel.class).queryResults();
                            } catch (Exception e) {
                                throw new ResultProcessingException("Could not parse response", e);
                            }
                        }
                        return null;
                    }

                    @Override
                    public void close() {
                    }

                    @Override
                    public String[] columns() {
                        return new String[0];
                    }
                };
            }

            @Override
            public Response<RowModel> execute(RowModelRequest query) {

                Holder holder = new Holder(getResponse(query.getStatement()));
                return new Response<RowModel>() {

                    @Override
                    public RowModel next() {
                        String r = holder.nextRecord();
                        if (r != null) {
                            try {
                                return new DefaultRowModel(mapper.readValue(r, ResultRowModel.class).queryResults(),
                                    columns());
                            } catch (Exception e) {
                                throw new ResultProcessingException("Could not parse response", e);
                            }
                        }
                        return null;
                    }

                    @Override
                    public void close() {
                    }

                    @Override
                    public String[] columns() {
                        return new String[0];
                    }
                };
            }

            @Override
            public Response<RowModel> execute(DefaultRequest query) {
                return null;
            }

            @Override
            public Response<GraphRowListModel> execute(GraphRowListModelRequest query) {
                return new Response<GraphRowListModel>() {

                    @Override
                    public DefaultGraphRowListModel next() {
                        throw new RuntimeException("not implemented");
                    }

                    @Override
                    public void close() {
                    }

                    @Override
                    public String[] columns() {
                        return new String[0];
                    }
                };
            }

            @Override
            public Response<RestModel> execute(RestModelRequest query) {
                return null; //TODO fix
            }
        };
    }

    @Override
    protected String getTypeSystemName() {
        throw new UnsupportedOperationException();
    }
}
