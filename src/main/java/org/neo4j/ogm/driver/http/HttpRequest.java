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
import org.neo4j.ogm.cypher.query.*;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.cypher.statement.ParameterisedStatements;
import org.neo4j.ogm.driver.Driver;
import org.neo4j.ogm.metadata.MappingException;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.session.request.RequestHandler;
import org.neo4j.ogm.session.response.*;
import org.neo4j.ogm.session.result.GraphRowModel;
import org.neo4j.ogm.session.result.RowModel;
import org.neo4j.ogm.session.result.RowQueryStatisticsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class HttpRequest implements RequestHandler {

    private final ObjectMapper mapper;
    private final Driver driver;
    private final Logger logger = LoggerFactory.getLogger(HttpRequest.class);

    public HttpRequest(ObjectMapper mapper, Driver driver) {
        this.driver = driver;
        this.mapper = mapper;
    }

    @Override
    public Neo4jResponse<GraphModel> execute(GraphModelQuery  query) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(query);
        Neo4jResponse<String> response = execute(list);
        return new GraphModelResponse(response, mapper);
    }

    @Override
    public Neo4jResponse<RowModel> execute(RowModelQuery query) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(query);
        Neo4jResponse<String> response = execute(list);
        return new RowModelResponse(response, mapper);
    }

    @Override
    public Neo4jResponse<GraphRowModel> execute(GraphRowModelQuery query) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(query);
        Neo4jResponse<String> response = execute(list);
        return new GraphRowModelResponse(response, mapper);
    }

    @Override
    public Neo4jResponse<String> execute(ParameterisedStatement statement) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(statement);
        return execute(list);
    }

    @Override
    public Neo4jResponse<RowQueryStatisticsResult> execute(RowModelQueryWithStatistics query) {
        List<ParameterisedStatement> list = new ArrayList<>();
        list.add(query);
        Neo4jResponse<String> response = execute(list);
        return new RowStatisticsResponse(response, mapper);
    }


    @Override
    public Neo4jResponse<String> execute(List<ParameterisedStatement> statementList) {
        try {
            String cypher = mapper.writeValueAsString(new ParameterisedStatements(statementList));
            // check if we have a statement. This is not ideal
            if (!cypher.contains("statement\":\"\"")) {    // not an empty statement
                logger.debug(cypher);
                return driver.execute(cypher);
            }
            return new EmptyResponse();
        } catch (JsonProcessingException jpe) {
            throw new MappingException("Could not create JSON due to " + jpe.getLocalizedMessage(),jpe);
        }
    }

//    public Neo4jResponse<String> execute(String cypher) {
//
//        HttpResponse jsonResponse = null;
//
//        try {
//            String url = this.url;
//
//            assert(url != null);
//
//            logger.debug("POST " + url + ", request: " + cypher);
//
//            HttpPost request = new HttpPost(url);
//            HttpEntity entity = new StringEntity(cypher,"UTF-8");
//
//            request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
//            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));
//
//            // http://tools.ietf.org/html/rfc7231#section-5.5.3
//            request.setHeader(new BasicHeader("User-Agent", "neo4j-ogm.java/1.0"));
//
//            HttpAuthorization.authorize(request, (Neo4jCredentials) driverConfig.getConfig("credentials"));
//
//            request.setEntity(entity);
//
//            CloseableHttpResponse response = httpClient.execute(request);
//
//            StatusLine statusLine = response.getStatusLine();
//            HttpEntity responseEntity = response.getEntity();
//
//            if (statusLine.getStatusCode() >= 300) {
//                throw new HttpResponseException(
//                        statusLine.getStatusCode(),
//                        statusLine.getReasonPhrase());
//            }
//            if (responseEntity == null) {
//                throw new ClientProtocolException("Response contains no content");
//            }
//
//            logger.debug("Response is OK, creating response handler");
//            jsonResponse = new HttpResponse(response);
//            return jsonResponse;
//
//        }
//        // the primary exception handler, will ensure all resources are properly closed
//        catch (Exception e) {
//            logger.warn("Caught response exception: " + e.getLocalizedMessage());
//            if (jsonResponse != null) {
//                jsonResponse.close();
//            }
//            throw new ResultProcessingException("Failed to execute request: " + cypher, e);
//        }
//    }


}
