/*
 * Copyright (c) 2002-2022 "Neo4j,"
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
package org.neo4j.ogm.drivers.http.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NoHttpResponseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.neo4j.ogm.config.Credentials;
import org.neo4j.ogm.config.ObjectMapperFactory;
import org.neo4j.ogm.drivers.http.response.GraphModelResponse;
import org.neo4j.ogm.drivers.http.response.GraphRowsModelResponse;
import org.neo4j.ogm.drivers.http.response.RestModelResponse;
import org.neo4j.ogm.drivers.http.response.RowModelResponse;
import org.neo4j.ogm.exception.ConnectionException;
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
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.request.Statements;
import org.neo4j.ogm.response.EmptyResponse;
import org.neo4j.ogm.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class HttpRequest implements Request {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequest.class);
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.objectMapper();
    private static final String JSON_PARSE_ERROR_EXCEPTION_MESSAGE = "Could not parse the servers response as JSON";

    private final String url;
    private final CloseableHttpClient httpClient;
    private final Credentials credentials;
    private final boolean readOnly;

    public HttpRequest(CloseableHttpClient httpClient, String url, Credentials credentials) {
        this(httpClient, url, credentials, false);
    }

    public HttpRequest(CloseableHttpClient httpClient, String url, Credentials credentials, boolean readOnly) {
        this.httpClient = httpClient;
        this.url = url;
        this.credentials = credentials;
        this.readOnly = readOnly;
    }

    @Override
    public Response<GraphModel> execute(GraphModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        } else {
            String cypher = cypherRequest(request);
            return new GraphModelResponse(executeRequest(cypher));
        }
    }

    @Override
    public Response<RowModel> execute(RowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        } else {
            String cypher = cypherRequest(request);
            return new RowModelResponse(executeRequest(cypher));
        }
    }

    @Override
    public Response<RowModel> execute(DefaultRequest query) {
        Statements statements = new Statements(query.getStatements());
        String cypher = cypherRequest(statements);
        return new RowModelResponse(executeRequest(cypher));
    }

    @Override
    public Response<GraphRowListModel> execute(GraphRowListModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        } else {
            String cypher = cypherRequest(request);
            return new GraphRowsModelResponse(executeRequest(cypher));
        }
    }

    @Override
    public Response<RestModel> execute(RestModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        } else {
            String cypher = cypherRequest(request);
            return new RestModelResponse(executeRequest(cypher));
        }
    }

    // we use the OBJECT_MAPPER to create the request string from the statement.
    // this driver is the only one that needs to do this, because the request format
    // is different for each type of request - GraphModelRequest/RowModelRequest, etc
    private String cypherRequest(Statement statement) {
        List<Statement> statementList = new ArrayList<>();
        statementList.add(statement);
        try {
            return OBJECT_MAPPER.writeValueAsString(new Statements(statementList));
        } catch (JsonProcessingException jpe) {
            throw new ResultProcessingException("Could not create JSON due to " + jpe.getLocalizedMessage(), jpe);
        }
    }

    private String cypherRequest(Statements statements) {
        try {
            return OBJECT_MAPPER.writeValueAsString(statements);
        } catch (JsonProcessingException jpe) {
            throw new ResultProcessingException("Could not create JSON due to " + jpe.getLocalizedMessage(), jpe);
        }
    }

    private CloseableHttpResponse executeRequest(String cypher) throws HttpRequestException {

        assert (url != null);

        HttpPost request = new HttpPost(url);

        request.setEntity(new StringEntity(cypher, "UTF-8"));
        request.setHeader("X-WRITE", readOnly ? "0" : "1");

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Thread: {}, url: {}, request: {}", Thread.currentThread().getId(), url, cypher);
        }

        return execute(httpClient, request, credentials);
    }

    public static CloseableHttpResponse execute(CloseableHttpClient httpClient, HttpRequestBase request,
        Credentials credentials) throws HttpRequestException {

        LOGGER.debug("Thread: {}, request: {}", Thread.currentThread().getId(), request);

        CloseableHttpResponse response;

        request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
        request.setHeader(new BasicHeader(HTTP.USER_AGENT, "neo4j-ogm.java/2.0"));
        request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));

        HttpAuthorization.authorize(request, credentials);

        // use defaults: 3 retries, 2 second wait between attempts
        RetryOnExceptionStrategy retryStrategy = new RetryOnExceptionStrategy();

        while (retryStrategy.shouldRetry()) {

            try {

                response = httpClient.execute(request);

                StatusLine statusLine = response.getStatusLine();
                HttpEntity responseEntity = response.getEntity();

                if (statusLine.getStatusCode() >= 300) {
                    String responseText = statusLine.getReasonPhrase();
                    if (responseEntity != null) {
                        responseText = parseError(EntityUtils.toString(responseEntity));
                        LOGGER.warn("Thread: {}, response: {}", Thread.currentThread().getId(), responseText);
                    }
                    throw new HttpResponseException(statusLine.getStatusCode(), responseText);
                }
                if (responseEntity == null) {
                    throw new ClientProtocolException("Response contains no content");
                }

                return response; // don't close response yet, it is not consumed!
            } catch (NoHttpResponseException nhre) {
                // if we didn't get a response at all, try again
                LOGGER.warn("Thread: {}, No response from server:  Retrying in {} milliseconds, retries left: {}",
                    Thread.currentThread().getId(), retryStrategy.getTimeToWait(), retryStrategy.numberOfTriesLeft);
                retryStrategy.errorOccurred();
            } catch (RetryException re) {
                throw new HttpRequestException(request, re);
            } catch (ClientProtocolException uhe) {
                throw new ConnectionException(request.getURI().toString(), uhe);
            } catch (IOException ioe) {
                throw new HttpRequestException(request, ioe);
            } catch (Exception exception) {
                // here we catch any exception we throw above (plus any we didn't throw ourselves),
                // log the problem, close any connection held by the request
                // and then rethrow the exception to the caller.
                LOGGER.warn("Thread: {}, exception: {}", Thread.currentThread().getId(),
                    exception.getCause().getLocalizedMessage());
                request.releaseConnection();
                throw exception;
            }
        }
        throw new RuntimeException("Fatal Exception: Should not have occurred!");
    }

    static class RetryOnExceptionStrategy {

        public static final int DEFAULT_RETRIES = 3;
        public static final long DEFAULT_WAIT_TIME_IN_MILLI = 2000;

        private int numberOfRetries;
        private int numberOfTriesLeft;
        private long timeToWait;

        RetryOnExceptionStrategy() {
            this(DEFAULT_RETRIES, DEFAULT_WAIT_TIME_IN_MILLI);
        }

        RetryOnExceptionStrategy(int numberOfRetries, long timeToWait) {
            this.numberOfRetries = numberOfRetries;
            numberOfTriesLeft = numberOfRetries;
            this.timeToWait = timeToWait;
        }

        /**
         * @return true if there are tries left
         */
        public boolean shouldRetry() {
            return numberOfTriesLeft > 0;
        }

        public void errorOccurred() {
            numberOfTriesLeft--;
            if (!shouldRetry()) {
                throw new RetryException("Retry Failed: Total " + numberOfRetries
                    + " attempts made at interval " + getTimeToWait()
                    + "ms");
            }
            waitUntilNextTry();
        }

        public long getTimeToWait() {
            return timeToWait;
        }

        private void waitUntilNextTry() {
            try {
                Thread.sleep(getTimeToWait());
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static String parseError(String responseBody) {
        try {
            final JsonNode responseNode = OBJECT_MAPPER.readTree(responseBody);
            final JsonNode errors = responseNode.findValue("errors");
            if (errors.elements().hasNext()) {
                final JsonNode errorNode = errors.elements().next();
                return errorNode.findValue("message").asText();
            } else {
                return responseBody;
            }
        } catch (JsonParseException e) {
            // Don't return the responseBody here as it is logged in #execute
            // See: https://www.owasp.org/index.php/Log_Injection, returning
            // it above should be rethought as well.
            return JSON_PARSE_ERROR_EXCEPTION_MESSAGE;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class RetryException extends RuntimeException {

        RetryException(String msg) {
            super(msg);
        }
    }
}
