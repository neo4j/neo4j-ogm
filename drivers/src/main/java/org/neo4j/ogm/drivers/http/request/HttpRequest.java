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

package org.neo4j.ogm.drivers.http.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.neo4j.ogm.authentication.Credentials;
import org.neo4j.ogm.drivers.http.response.GraphModelResponse;
import org.neo4j.ogm.drivers.http.response.GraphRowsModelResponse;
import org.neo4j.ogm.drivers.http.response.RowModelResponse;
import org.neo4j.ogm.drivers.http.response.RowStatisticsModelResponse;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.json.ObjectMapperFactory;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.model.RowStatisticsModel;
import org.neo4j.ogm.request.*;
import org.neo4j.ogm.response.EmptyResponse;
import org.neo4j.ogm.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class HttpRequest implements Request {

    private static final ObjectMapper mapper = ObjectMapperFactory.objectMapper();

    private final String url;
    private final CloseableHttpClient httpClient;
    private final Credentials credentials;

    private final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    public HttpRequest(CloseableHttpClient httpClient, String url, Credentials credentials) {
        this.httpClient = httpClient;
        this.url = url;
        this.credentials = credentials;
    }

    @Override
    public Response<GraphModel> execute(GraphModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        else {
            String cypher = cypherRequest(request);
            try {
                return new GraphModelResponse(executeRequest(cypher));
            } catch (Exception e) {
                throw new ResultProcessingException("Could not parse response", e);
            }
        }
    }

    @Override
    public Response<RowModel> execute(RowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        else {
            String cypher = cypherRequest(request);
            try {
                return new RowModelResponse(executeRequest(cypher));
            } catch (Exception e) {
                throw new ResultProcessingException("Could not parse response", e);
            }
        }
    }

    @Override
    public Response<RowModel> execute(DefaultRequest query) {
        Statements statements = new Statements(query.getStatements());
        String cypher = cypherRequest(statements);
        try {
            return new RowModelResponse(executeRequest(cypher));
        }
        catch (Exception e) {
            throw new ResultProcessingException("Could not parse response", e);
        }
    }

    @Override
    public Response<GraphRowListModel> execute(GraphRowListModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        else {
            String cypher = cypherRequest(request);
            try {
                return new GraphRowsModelResponse(executeRequest(cypher));
            } catch (Exception e) {
                throw new ResultProcessingException("Could not parse response", e);
            }
        }

    }

    @Override
    public Response<RowStatisticsModel> execute(RowStatisticsModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        else {
            String cypher = cypherRequest(request);
            try {
                return new RowStatisticsModelResponse(executeRequest(cypher));
            } catch (Exception e) {
                throw new ResultProcessingException("Could not parse response", e);
            }
        }
    }


    // we use the mapper to create the request string from the statement.
    // this driver is the only one that needs to do this, because the request format
    // is different for each type of request - GraphModelRequest/RowModelRequest, etc
    private String cypherRequest(Statement statement) {
        List<Statement> statementList = new ArrayList<>();
        statementList.add(statement);
        try {
            return mapper.writeValueAsString(new Statements(statementList));
        } catch (JsonProcessingException jpe) {
            throw new ResultProcessingException("Could not create JSON due to " + jpe.getLocalizedMessage(),jpe);
        }
    }

    private String cypherRequest(Statements statements) {
        try {
            return mapper.writeValueAsString(statements);
        } catch (JsonProcessingException jpe) {
            throw new ResultProcessingException("Could not create JSON due to " + jpe.getLocalizedMessage(),jpe);
        }
    }

    private CloseableHttpResponse executeRequest(String cypher) {

        CloseableHttpResponse response = null;

        try {
            String url = this.url;

            assert(url != null);

            logger.debug("POST {}, request {}", url, cypher);

            HttpPost request = new HttpPost(url);
            HttpEntity entity = new StringEntity(cypher,"UTF-8");

            request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));

            // http://tools.ietf.org/html/rfc7231#section-5.5.3
            request.setHeader(new BasicHeader("User-Agent", "neo4j-ogm.java/1.0"));

            HttpAuthorization.authorize(request, credentials);

            request.setEntity(entity);

            response = httpClient.execute(request);

            StatusLine statusLine = response.getStatusLine();
            HttpEntity responseEntity = response.getEntity();

            if (statusLine.getStatusCode() >= 300) {
                throw new HttpResponseException(
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }
            if (responseEntity == null) {
                throw new ClientProtocolException("Response contains no content");
            }

            logger.debug("Response is OK");
            return response;
        }
        // the primary exception handler, will ensure all resources are properly closed
        catch (Exception e) {
            logger.warn("Caught response exception: {}", e.getLocalizedMessage());
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ioe) {
                    throw new ResultProcessingException("Failed to close response: ", e);
                }
            }
            throw new ResultProcessingException("Failed to execute request: " + cypher, e);
        }
    }


}
