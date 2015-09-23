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

package org.neo4j.ogm.driver.http;

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
import org.neo4j.ogm.authentication.Neo4jCredentials;
import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.cypher.query.GraphRowModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQueryWithStatistics;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.cypher.statement.ParameterisedStatements;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.session.request.Request;
import org.neo4j.ogm.session.response.*;
import org.neo4j.ogm.session.response.model.GraphModel;
import org.neo4j.ogm.session.response.model.GraphRowModel;
import org.neo4j.ogm.session.response.model.RowModel;
import org.neo4j.ogm.session.response.model.RowStatisticsModel;
import org.neo4j.ogm.session.result.ResultProcessingException;
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

    private static final ObjectMapper mapper = new ObjectMapper();


    private final String url;
    private final CloseableHttpClient httpClient;
    private final Neo4jCredentials credentials;


    private final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    public HttpRequest(CloseableHttpClient httpClient, String url, Neo4jCredentials credentials) {
        this.httpClient = httpClient;
        this.url = url;
        this.credentials = credentials;
    }

    @Override
    public Response<GraphModel> execute(GraphModelQuery query) {
        // what should happen here?

        // the actual driver should make a request.

        // it should pass in the appropriate response adapter so that the
        // response will return objects of the correct type.

        // for example, the embedded adapter returns Result objects natively,
        // so it should implement an adapter to transform a Result.next() to a GraphModel

        // whereas, the http adapter returns a response in the way we request, so
        // we must tell it what we want, "GraphModel", etc, execute the query,
        // and simply parse the returned JSON accordingly
        return new GraphModelResponse(execute((ParameterisedStatement) query), mapper);
    }

    @Override
    public Response<RowModel> execute(RowModelQuery query) {
        return new RowModelResponse(execute((ParameterisedStatement) query), mapper);
    }

    @Override
    public Response<GraphRowModel> execute(GraphRowModelQuery query) {
        return new GraphRowModelResponse(execute((ParameterisedStatement) query), mapper);
    }

    @Override
    public Response<RowStatisticsModel> execute(RowModelQueryWithStatistics query) {
        return new RowStatisticsResponse(execute((ParameterisedStatement) query), mapper);
    }

    private Response<String> execute(ParameterisedStatement statement) {

        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(statement);

        try {
            String cypher = mapper.writeValueAsString(new ParameterisedStatements(list));
            // check if we have a statement. This is not ideal
            if (!cypher.contains("statement\":\"\"")) {    // not an empty statement
                logger.debug(cypher);
                return new HttpResponse(execute(cypher));
            }
            return new EmptyResponse();
        } catch (JsonProcessingException jpe) {
            throw new MappingException("Could not create JSON due to " + jpe.getLocalizedMessage(),jpe);
        }
    }

    private CloseableHttpResponse execute(String cypher) {

        CloseableHttpResponse response = null;

        try {
            String url = this.url;

            assert(url != null);

            logger.debug("POST " + url + ", request: " + cypher);

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
            logger.warn("Caught response exception: " + e.getLocalizedMessage());
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
