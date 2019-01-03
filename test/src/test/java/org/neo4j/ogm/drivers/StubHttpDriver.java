/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    @Override
    public void close() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Transaction newTransaction(Transaction.Type type, Iterable<String> bookmarks) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Request request() {

        return new Request() {

            private final String[] json = getResponse();
            private int count = 0;

            private String nextRecord() {
                if (count < json.length) {
                    String r = json[count];
                    count++;
                    return r;
                }
                return null;
            }

            @Override
            public Response<GraphModel> execute(GraphModelRequest qry) {

                return new Response<GraphModel>() {

                    @Override
                    public GraphModel next() {
                        String r = nextRecord();
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
                return new Response<RowModel>() {

                    @Override
                    public RowModel next() {
                        String r = nextRecord();
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
}
