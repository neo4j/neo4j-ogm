/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.ogm.config.ObjectMapperFactory;
import org.neo4j.ogm.drivers.bolt.response.GraphModelResponse;
import org.neo4j.ogm.drivers.bolt.response.GraphRowModelResponse;
import org.neo4j.ogm.drivers.bolt.response.RestModelResponse;
import org.neo4j.ogm.drivers.bolt.response.RowModelResponse;
import org.neo4j.ogm.drivers.bolt.transaction.BoltTransaction;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.*;
import org.neo4j.ogm.response.EmptyResponse;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 * @author Luanne Misquitta
 */
public class BoltRequest implements Request {

    private final TransactionManager transactionManager;

    private static final ObjectMapper mapper = ObjectMapperFactory.objectMapper();

    private final Logger LOGGER = LoggerFactory.getLogger(BoltRequest.class);


    public BoltRequest(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Response<GraphModel> execute(GraphModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new GraphModelResponse(executeRequest(request), transactionManager);
    }

    @Override

    public Response<RowModel> execute(RowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new RowModelResponse(executeRequest(request), transactionManager);
    }

    @Override
    public Response<RowModel> execute(DefaultRequest query) {
        final List<RowModel> rowmodels = new ArrayList<>();
        String[] columns = null;
        for (Statement statement : query.getStatements()) {
            StatementResult result = executeRequest(statement);
            if (columns == null) {
                List<String> columnSet = result.keys();
                columns = columnSet.toArray(new String[columnSet.size()]);
            }
            RowModelResponse rowModelResponse = new RowModelResponse(result, transactionManager);
            RowModel model;
            while ((model = rowModelResponse.next()) != null) {
                rowmodels.add(model);
            }
            result.consume();
        }

        final String[] finalColumns = columns;
        return new Response<RowModel>() {
            int currentRow = 0;

            @Override
            public RowModel next() {
                if (currentRow < rowmodels.size()) {
                    return rowmodels.get(currentRow++);
                }
                return null;
            }

            @Override
            public void close() {
            }

            @Override
            public String[] columns() {
                return finalColumns;
            }
        };
    }

    @Override
    public Response<GraphRowListModel> execute(GraphRowListModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new GraphRowModelResponse(executeRequest(request), transactionManager);
    }


    @Override
    public Response<RestModel> execute(RestModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new RestModelResponse(executeRequest(request), transactionManager);
    }

    private StatementResult executeRequest(Statement request) {
        BoltTransaction tx;
        try {
            String params = mapper.writeValueAsString(request.getParameters());
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
            };
            HashMap<String, Object> parameterMap = mapper.readValue(params, typeRef);

            LOGGER.info("Request: {} with params {}", request.getStatement(), parameterMap);

            if (transactionManager.getCurrentTransaction() == null) {
                org.neo4j.ogm.transaction.Transaction autoCommitTx = transactionManager.openTransaction();
                tx = (BoltTransaction) autoCommitTx;
                StatementResult statementResult = tx.nativeBoltTransaction().run(request.getStatement(), parameterMap);
                tx.commit();
                tx.close();
                return statementResult;
            }
            tx = (BoltTransaction) transactionManager.getCurrentTransaction();
            return tx.nativeBoltTransaction().run(request.getStatement(), parameterMap);
        } catch (CypherException | ConnectionException ce) {
            throw ce;
        } catch (ClientException ce) {
            tx = (BoltTransaction) transactionManager.getCurrentTransaction();
            if (tx != null) {
                tx.rollback();
            }
            throw new CypherException("Error executing Cypher", ce, ce.neo4jErrorCode(), ce.getMessage());
        } catch (Exception e) {
            tx = (BoltTransaction) transactionManager.getCurrentTransaction();
            if (tx != null) {
                tx.rollback();
            }
            throw new RuntimeException(e);
        }
    }
}
