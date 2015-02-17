/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.session.request;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.neo4j.ogm.session.response.JsonResponse;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultRequest implements Neo4jRequest<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequest.class);

    private final CloseableHttpClient httpClient;

    public DefaultRequest(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Neo4jResponse<String> execute(String url, String cypherQuery) {

        try {

            LOGGER.info("POST " + url + ", request: " + cypherQuery);

            HttpPost request = new HttpPost(url);
            HttpEntity entity = new StringEntity(cypherQuery);

            request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));
            request.setEntity(entity);

            HttpResponse response = httpClient.execute(request);

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

            LOGGER.info("response is OK, creating response handler");
            return new JsonResponse(responseEntity.getContent());


        }
        catch (Exception e) {
            System.out.println("caught response exception: " + e.getLocalizedMessage());
            throw new ResultProcessingException("Failed to execute request: " + cypherQuery, e);
        }
    }
}
