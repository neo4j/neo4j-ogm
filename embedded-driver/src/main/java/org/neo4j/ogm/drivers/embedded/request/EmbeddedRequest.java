/*
 * Copyright (c) 2002-2021 "Neo4j,"
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
package org.neo4j.ogm.drivers.embedded.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.QueryExecutionException;
import org.neo4j.graphdb.Result;
import org.neo4j.ogm.driver.ParameterConversion;
import org.neo4j.ogm.drivers.embedded.driver.EmbeddedEntityAdapter;
import org.neo4j.ogm.drivers.embedded.response.GraphModelResponse;
import org.neo4j.ogm.drivers.embedded.response.GraphRowModelResponse;
import org.neo4j.ogm.drivers.embedded.response.RestModelResponse;
import org.neo4j.ogm.drivers.embedded.response.RowModelResponse;
import org.neo4j.ogm.drivers.embedded.transaction.EmbeddedTransaction;
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
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class EmbeddedRequest implements Request {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedRequest.class);

    private final GraphDatabaseService graphDatabaseService;
    private final Transaction transaction;

    private final ParameterConversion parameterConversion;

    private final EmbeddedEntityAdapter entityAdapter;

    private final Function<String, String> cypherModification;

    public EmbeddedRequest(GraphDatabaseService graphDatabaseService,
        Transaction transaction,
        ParameterConversion parameterConversion, EmbeddedEntityAdapter entityAdapter,
        Function<String, String> cypherModification
    ) {
        this.graphDatabaseService = graphDatabaseService;
        this.transaction = transaction;
        this.parameterConversion = parameterConversion;
        this.entityAdapter = entityAdapter;
        this.cypherModification = cypherModification;
    }

    @Override
    public Response<GraphModel> execute(GraphModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new GraphModelResponse(executeRequest(request), entityAdapter);
    }

    @Override
    public Response<RowModel> execute(RowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new RowModelResponse(executeRequest(request), entityAdapter);
    }

    @Override
    public Response<RowModel> execute(DefaultRequest query) {
        //TODO this is a hack to get the embedded driver to work with executing multiple statements
        final List<RowModel> rowmodels = new ArrayList<>();
        String[] columns = null;
        for (Statement statement : query.getStatements()) {
            Result result = executeRequest(statement);
            if (columns == null) {
                columns = result.columns().toArray(new String[result.columns().size()]);
            }
            RowModelResponse rowModelResponse = new RowModelResponse(result, entityAdapter);
            RowModel model;
            while ((model = rowModelResponse.next()) != null) {
                rowmodels.add(model);
            }
            result.close();
        }

        final String[] finalColumns = columns;
        return new Response<>() {
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
        return new GraphRowModelResponse(executeRequest(request), entityAdapter);
    }

    @Override
    public Response<RestModel> execute(RestModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        return new RestModelResponse(executeRequest(request), entityAdapter);
    }

    private Result executeRequest(Statement request) {

        try {
            Map<String, Object> parameterMap = this.parameterConversion.convertParameters(request.getParameters());
            String cypher = cypherModification.apply(request.getStatement());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Request: {} with params {}", cypher, parameterMap);
            }

            if (this.transaction == null) {
                throw new RuntimeException("Cannot execute request without transaction!");
            } else {
                return ((EmbeddedTransaction) transaction).getNativeTransaction().execute(cypher, parameterMap);
            }
        } catch (QueryExecutionException qee) {
            throw new CypherException(qee.getStatusCode(), qee.getMessage(), qee);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
