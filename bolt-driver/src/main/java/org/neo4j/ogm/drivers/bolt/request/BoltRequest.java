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

package org.neo4j.ogm.drivers.bolt.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.driver.v1.exceptions.DatabaseException;
import org.neo4j.driver.v1.exceptions.TransientException;
import org.neo4j.ogm.driver.ParameterConversion;
import org.neo4j.ogm.drivers.bolt.driver.BoltEntityAdapter;
import org.neo4j.ogm.drivers.bolt.response.GraphModelResponse;
import org.neo4j.ogm.drivers.bolt.response.GraphRowModelResponse;
import org.neo4j.ogm.drivers.bolt.response.RestModelResponse;
import org.neo4j.ogm.drivers.bolt.response.RowModelResponse;
import org.neo4j.ogm.drivers.bolt.transaction.BoltTransaction;
import org.neo4j.ogm.exception.CypherException;
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
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class BoltRequest implements Request {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoltRequest.class);

    private final TransactionManager transactionManager;

    private final ParameterConversion parameterConversion;

    private final BoltEntityAdapter entityAdapter;

    private final Function<String, String> cypherModification;

    public BoltRequest(TransactionManager transactionManager, ParameterConversion parameterConversion, BoltEntityAdapter entityAdapter,
        Function<String, String> cypherModification) {
        this.transactionManager = transactionManager;
        this.parameterConversion = parameterConversion;
        this.entityAdapter = entityAdapter;
        this.cypherModification = cypherModification;
    }

    @Override
    public Response<GraphModel> execute(GraphModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new GraphModelResponse(executeRequest(request), transactionManager, entityAdapter);
    }

    @Override

    public Response<RowModel> execute(RowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new RowModelResponse(executeRequest(request), transactionManager, entityAdapter);
    }

    @Override
    public Response<RowModel> execute(DefaultRequest query) {
        final List<RowModel> rowModels = new ArrayList<>();
        String[] columns = null;
        for (Statement statement : query.getStatements()) {

            StatementResult result = executeRequest(statement);

            if (columns == null) {
                try {
                    List<String> columnSet = result.keys();
                    columns = columnSet.toArray(new String[columnSet.size()]);
                } catch (ClientException e) {
                    throw new CypherException(e.code(), e.getMessage(), e);
                }
            }

            try (RowModelResponse rowModelResponse = new RowModelResponse(result, transactionManager, entityAdapter)) {
                RowModel model;
                while ((model = rowModelResponse.next()) != null) {
                    rowModels.add(model);
                }
                result.consume();
            }
        }

        return new MultiStatementBasedResponse(columns, rowModels);
    }

    private static class MultiStatementBasedResponse implements  Response<RowModel> {
        // This implementation is not good, but it preserved the current behaviour while fixing another bug.
        // While the statements executed in org.neo4j.ogm.drivers.bolt.request.BoltRequest.execute(org.neo4j.ogm.request.DefaultRequest)
        // might return different columns, only the ones of the first result are used. :(
        private final String[] columns;
        private final List<RowModel> rowModels;

        private int currentRow = 0;

        MultiStatementBasedResponse(String[] columns, List<RowModel> rowModels) {
            this.columns = columns;
            this.rowModels = rowModels;
        }

        @Override
        public RowModel next() {
            if (currentRow < rowModels.size()) {
                return rowModels.get(currentRow++);
            }
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public String[] columns() {
            return this.columns;
        }
    }

    @Override
    public Response<GraphRowListModel> execute(GraphRowListModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new GraphRowModelResponse(executeRequest(request), transactionManager, entityAdapter);
    }

    @Override
    public Response<RestModel> execute(RestModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new RestModelResponse(executeRequest(request), transactionManager, entityAdapter);
    }

    private StatementResult executeRequest(Statement request) {
        try {
            Map<String, Object> parameterMap = this.parameterConversion.convertParameters(request.getParameters());
            String cypher = cypherModification.apply(request.getStatement());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Request: {} with params {}", cypher, parameterMap);
            }

            BoltTransaction tx = (BoltTransaction) transactionManager.getCurrentTransaction();
            return tx.nativeBoltTransaction().run(cypher, parameterMap);
        } catch (ClientException | DatabaseException | TransientException ce) {
            throw new CypherException(ce.code(), ce.getMessage(), ce);
        }
    }
}
