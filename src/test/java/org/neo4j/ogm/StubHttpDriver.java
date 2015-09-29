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
import org.neo4j.ogm.cypher.query.GraphModelRequest;
import org.neo4j.ogm.cypher.query.GraphRowModelRequest;
import org.neo4j.ogm.cypher.query.RowModelRequest;
import org.neo4j.ogm.cypher.query.RowModelStatisticsRequest;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.driver.config.DriverConfig;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.request.Request;
import org.neo4j.ogm.session.response.Response;
import org.neo4j.ogm.session.response.model.GraphModel;
import org.neo4j.ogm.session.response.model.GraphRowModel;
import org.neo4j.ogm.session.response.model.RowModel;
import org.neo4j.ogm.session.response.model.RowStatisticsModel;
import org.neo4j.ogm.session.result.GraphModelResult;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.neo4j.ogm.session.result.RowModelResult;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;

/**
 * @author Vince Bickers
 */
public abstract class StubHttpDriver implements Driver {

    private final ObjectMapper mapper = new ObjectMapper();

    protected abstract String[] getResponse();

    @Override
    public void close() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void configure(DriverConfig config) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Transaction newTransaction(MappingContext context, TransactionManager tx, boolean autoCommit) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object getConfig(String key) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public TransactionManager transactionManager() {
       return null;
    }

    @Override
    public void setTransactionManager(TransactionManager transactionManager) {
        ;
    }

    @Override
    public Request requestHandler() {

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
                                return mapper.readValue(r, GraphModelResult.class).getGraph();
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
                                return new RowModel(mapper.readValue(r, RowModelResult.class).getRow());
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
            public Response<GraphRowModel> execute(GraphRowModelRequest query) {
                return new Response<GraphRowModel>() {

                    @Override
                    public GraphRowModel next() {
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
            public Response<RowStatisticsModel> execute(RowModelStatisticsRequest query) {
                return new Response<RowStatisticsModel>() {

                    @Override
                    public RowStatisticsModel next() {
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
        };
    }



}
