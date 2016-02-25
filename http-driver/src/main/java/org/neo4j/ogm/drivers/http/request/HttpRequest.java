/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.drivers.http.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.neo4j.ogm.authentication.Credentials;
import org.neo4j.ogm.drivers.http.response.GraphModelResponse;
import org.neo4j.ogm.drivers.http.response.GraphRowsModelResponse;
import org.neo4j.ogm.drivers.http.response.RestModelResponse;
import org.neo4j.ogm.drivers.http.response.RowModelResponse;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.json.ObjectMapperFactory;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.request.*;
import org.neo4j.ogm.response.EmptyResponse;
import org.neo4j.ogm.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class HttpRequest implements Request {

    private static final ObjectMapper mapper = ObjectMapperFactory.objectMapper();
    private static final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    private final String url;
    private final CloseableHttpClient httpClient;
    private final Credentials credentials;

    public HttpRequest(CloseableHttpClient httpClient, String url, Credentials credentials) {
        this.httpClient = httpClient;
        this.url = url;
        this.credentials = credentials;
    }

    @Override
    public Response<GraphModel> execute(GraphModelRequest request) {
        CloseableHttpResponse response = null;
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        } else {
            String cypher = cypherRequest(request);
            try {
                response = executeRequest( cypher );
                return new GraphModelResponse(response);
            }
            catch (CypherException ce) {
                throw ce;
            }
            catch (Exception e) {
                throw new ResultProcessingException("Could not parse response", e);
            }
        }
    }

    @Override
    public Response<RowModel> execute(RowModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        } else {
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
        } catch (Exception e) {
            throw new ResultProcessingException("Could not parse response", e);
        }
    }

    @Override
    public Response<GraphRowListModel> execute(GraphRowListModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        } else {
            String cypher = cypherRequest(request);
            try {
                return new GraphRowsModelResponse(executeRequest(cypher));
            } catch (Exception e) {
                throw new ResultProcessingException("Could not parse response", e);
            }
        }

    }

    @Override
    public Response<RestModel> execute(RestModelRequest request) {
        if (request.getStatement().length() == 0) {
            return new EmptyResponse();
        }
        else {
            String cypher = cypherRequest(request);
            try {
                return new RestModelResponse(executeRequest(cypher));
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
            throw new ResultProcessingException("Could not create JSON due to " + jpe.getLocalizedMessage(), jpe);
        }
    }

    private String cypherRequest(Statements statements) {
        try {
            return mapper.writeValueAsString(statements);
        } catch (JsonProcessingException jpe) {
            throw new ResultProcessingException("Could not create JSON due to " + jpe.getLocalizedMessage(), jpe);
        }
    }

    private CloseableHttpResponse executeRequest(String cypher) {

        String url = this.url;

        assert (url != null);

        logger.info("POST {}, request {}", url, cypher);

        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(cypher, "UTF-8"));

        return execute(httpClient, request, credentials);
    }

    public static CloseableHttpResponse execute(CloseableHttpClient httpClient, HttpRequestBase request, Credentials credentials) {

        CloseableHttpResponse response = null;

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
                    if (responseEntity != null) {
                        String responseText = EntityUtils.toString(responseEntity);
                        logger.debug("Response Status: {} response: {}", statusLine.getStatusCode(), responseText);
                        EntityUtils.consume(responseEntity);
                    }
                    throw new HttpResponseException(
                            statusLine.getStatusCode(),
                            statusLine.getReasonPhrase());
                }
                if (responseEntity == null) {
                    throw new ClientProtocolException("Response contains no content");
                }

                logger.debug("Response is OK");
                return response;

            } catch (NoHttpResponseException nhre) {
                try {
                    logger.debug("No response from server.  Retrying in {} milliseconds, retries left: {}", retryStrategy.getTimeToWait(), retryStrategy.numberOfTriesLeft);
                    retryStrategy.errorOccured();
                } catch (Exception e) {
                    throw new ResultProcessingException("Request retry has failed", e);
                }
            }
            // the catch-all exception handler, will ensure all resources are properly closed in the event we cannot proceed
            // or there is a problem parsing the response from the server.
            catch (Exception e) {
                request.releaseConnection();
                logger.warn("Caught response exception: {}", e.getLocalizedMessage());
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException ioe) {
                        throw new ResultProcessingException("Failed to close response: ", e);
                    }
                }

                throw new ResultProcessingException("Failed to execute request", e);
            }
        }
        request.releaseConnection();
        throw new RuntimeException("Fatal Exception: Should not have occurred!");
    }

    static class RetryOnExceptionStrategy {

        public static final int DEFAULT_RETRIES = 3;
        public static final long DEFAULT_WAIT_TIME_IN_MILLI = 2000;

        private int numberOfRetries;
        private int numberOfTriesLeft;
        private long timeToWait;

        public RetryOnExceptionStrategy() {
            this(DEFAULT_RETRIES, DEFAULT_WAIT_TIME_IN_MILLI);
        }

        public RetryOnExceptionStrategy(int numberOfRetries, long timeToWait) {
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

        public void errorOccured() throws Exception {
            numberOfTriesLeft--;
            if (!shouldRetry()) {
                throw new Exception("Retry Failed: Total " + numberOfRetries
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
}
