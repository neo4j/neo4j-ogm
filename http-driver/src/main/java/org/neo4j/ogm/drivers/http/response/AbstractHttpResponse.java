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

package org.neo4j.ogm.drivers.http.response;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.config.ObjectMapperFactory;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.response.model.QueryStatisticsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 * @author Luanne Misquitta
 */
public abstract class AbstractHttpResponse<T> {

    private final InputStream results;
    private final JsonParser bufferParser;
    private final ObjectMapper mapper = ObjectMapperFactory.objectMapper();
    private final TokenBuffer buffer;
    private final Class<T> resultClass;
    private final CloseableHttpResponse httpResponse;

    private String[] columns;
    private QueryStatistics queryStatistics;
    private JsonNode responseNode;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHttpResponse.class);

    public AbstractHttpResponse(CloseableHttpResponse httpResponse, Class<T> resultClass) {

        this.resultClass = resultClass;
        try {
            this.httpResponse = httpResponse;
            this.results = httpResponse.getEntity().getContent();
            JsonParser parser = ObjectMapperFactory.jsonFactory().createParser(results);
            buffer = new TokenBuffer(parser);
            //Copy the contents of the response into the token buffer.
            //This is so that we do not have to serialize the response to textual json while we get to the end of the stream to check for errors
            parser.nextToken();
            buffer.copyCurrentStructure(parser);
            bufferParser = buffer.asParser();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        } finally {
            close(); //We are done with the InputStream
        }
        initialise();
    }

    private void initialise() {
        try {
            responseNode = mapper.readTree(buffer.asParser());
            LOGGER.debug("Response: {}", responseNode);
            JsonNode errors = responseNode.findValue("errors");
            if (errors.elements().hasNext()) {
                JsonNode errorNode = errors.elements().next();
                throw new CypherException("Error executing Cypher " + errorNode.findValue("code"), errorNode.findValue("code").asText(), errorNode.findValue("message").asText());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public T nextDataRecord(String key) {
        JsonToken token;
        try {
            while ((token = bufferParser.nextToken()) != null) {
                if (JsonToken.FIELD_NAME.equals(token)) {
                    if (key.equals(bufferParser.getCurrentName())) {
                        return mapper.readValue(bufferParser, resultClass);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the first set of columns from the JSON response.
     * Note that the current implementation expects that columns be standard across all statements in a Cypher transaction.
     *
     * @return the first set of columns from a JSON response
     */
    public String[] columns() {
        if (columns == null) {
            List<String> columnsList = new ArrayList<>();
            List<JsonNode> columnsNodes = responseNode.findValues("columns");
            if (columnsNodes != null && columnsNodes.size() > 0) {
                JsonNode firstColumnsNode = columnsNodes.get(0);
                for (JsonNode columnNode : firstColumnsNode) {
                    columnsList.add(columnNode.asText());
                }
                columns = new String[columnsList.size()];
                columns = columnsList.toArray(columns);
            }
        }
        return columns;
    }

    /**
     * Extract stats from the response if present
     *
     * @return queryStatistics or null if the response does not contain it
     */
    public QueryStatistics statistics() {
        if (queryStatistics == null) {
            List<JsonNode> statsNodes = responseNode.findValues("stats");
            try {
                if (statsNodes != null && statsNodes.size() > 0) {
                    queryStatistics = mapper.treeToValue(statsNodes.get(0), QueryStatisticsModel.class);
                }
            } catch (JsonProcessingException jsonException) {
                throw new RuntimeException(jsonException);
            }
        }
        return queryStatistics;
    }

    private void close() {
        try {
            LOGGER.debug("Thread {}: Releasing HttpResponse", Thread.currentThread().getId());
            results.close();
            httpResponse.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
