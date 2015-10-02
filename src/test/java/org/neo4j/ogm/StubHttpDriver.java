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
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.model.Graph;
import org.neo4j.ogm.api.model.GraphRows;
import org.neo4j.ogm.api.model.Row;
import org.neo4j.ogm.api.model.RowStatistics;
import org.neo4j.ogm.api.request.*;
import org.neo4j.ogm.api.response.Response;
import org.neo4j.ogm.api.transaction.Transaction;
import org.neo4j.ogm.api.transaction.TransactionManager;
import org.neo4j.ogm.spi.ServiceConfiguration;
import org.neo4j.ogm.driver.impl.model.GraphRowsModel;
import org.neo4j.ogm.driver.impl.model.RowModel;
import org.neo4j.ogm.driver.impl.model.RowStatisticsModel;
import org.neo4j.ogm.driver.impl.result.ResultGraphModel;
import org.neo4j.ogm.driver.impl.result.ResultProcessingException;
import org.neo4j.ogm.driver.impl.result.ResultRowModel;
import org.neo4j.ogm.spi.ServiceConfiguration;

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
    public void configure(ServiceConfiguration config) {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Transaction newTransaction() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Object getConfig(String key) {
        throw new RuntimeException("not implemented");
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
            public Response<Graph> execute(GraphModelRequest qry) {

                return new Response<Graph>() {

                    @Override
                    public Graph next() {
                        String r = nextRecord();
                        if (r != null) {
                            try {
                                return mapper.readValue(r, ResultGraphModel.class).model();
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
            public Response<Row> execute(RowModelRequest query) {
                return new Response<Row>() {

                    @Override
                    public Row next() {
                        String r = nextRecord();
                        if (r != null) {
                            try {
                                return new RowModel(mapper.readValue(r, ResultRowModel.class).model());
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
            public Response<GraphRows> execute(GraphRowModelRequest query) {
                return new Response<GraphRows>() {

                    @Override
                    public GraphRowsModel next() {
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
            public Response<RowStatistics> execute(RowModelStatisticsRequest query) {
                return new Response<RowStatistics>() {

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
