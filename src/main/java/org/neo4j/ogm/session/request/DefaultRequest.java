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

package org.neo4j.ogm.session.request;

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
import org.neo4j.ogm.authentication.CredentialsService;
import org.neo4j.ogm.authentication.HttpRequestAuthorization;
import org.neo4j.ogm.authentication.Neo4jCredentials;
import org.neo4j.ogm.authentication.UsernamePasswordCredentials;
import org.neo4j.ogm.session.response.JsonResponse;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class DefaultRequest implements Neo4jRequest<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequest.class);

    private final CloseableHttpClient httpClient;
    private final Neo4jCredentials credentials;

    public DefaultRequest(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
        this.credentials = CredentialsService.userNameAndPassword();
    }

    public DefaultRequest(CloseableHttpClient httpClient, UsernamePasswordCredentials usernamePasswordCredentials) {
        this.httpClient = httpClient;
        this.credentials = usernamePasswordCredentials;
    }

    public Neo4jResponse<String> execute(String url, String cypherQuery) {

        JsonResponse jsonResponse = null;

        try {

            LOGGER.debug("POST " + url + ", request: " + cypherQuery);

            HttpPost request = new HttpPost(url);
            HttpEntity entity = new StringEntity(cypherQuery,"UTF-8");

            request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));

            // http://tools.ietf.org/html/rfc7231#section-5.5.3
            request.setHeader(new BasicHeader("User-Agent", "neo4j-ogm.java/1.0"));

            HttpRequestAuthorization.authorize(request, credentials);

            request.setEntity(entity);

            CloseableHttpResponse response = httpClient.execute(request);

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

            LOGGER.debug("Response is OK, creating response handler");
            jsonResponse = new JsonResponse(response);
            return jsonResponse;

        }
        // the primary exception handler, will ensure all resources are properly closed
        catch (Exception e) {
            LOGGER.warn("Caught response exception: " + e.getLocalizedMessage());
            if (jsonResponse != null) {
                jsonResponse.close();
            }
            throw new ResultProcessingException("Failed to execute request: " + cypherQuery, e);
        }
    }


}
