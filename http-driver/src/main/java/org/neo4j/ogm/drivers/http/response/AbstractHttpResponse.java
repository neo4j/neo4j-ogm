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
package org.neo4j.ogm.drivers.http.response;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.config.ObjectMapperFactory;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.response.model.QueryStatisticsModel;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;

/**
 * NOTE: Both columns and statistics only work on the <strong>FIRST</strong> entry of the results array. That has been
 * the case at least since OGM 3.0.
 * Queries that contain multiple statements with possible a distinct set of columns and statistic, won't work correctly.
 *
 * @author Vince Bickers
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public abstract class AbstractHttpResponse<T> implements AutoCloseable {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private final ObjectMapper mapper = ObjectMapperFactory.objectMapper();

    private final Class<T> resultClass;
    private final String[] columns;
    private final QueryStatistics queryStatistics;

    private final ResultNodeIterator results;

    AbstractHttpResponse(CloseableHttpResponse httpResponse, Class<T> resultClass) {
        this(httpResponse, resultClass, true);
    }

    AbstractHttpResponse(CloseableHttpResponse httpResponse, Class<T> resultClass, boolean flatMapData) {

        this.resultClass = resultClass;

        try (
            CloseableHttpResponse httpResponseToClose = httpResponse;
            InputStream inputStreamOfResponse = httpResponseToClose.getEntity().getContent();
            TokenBuffer bufferedResponse = createTokenBuffer(inputStreamOfResponse);
        ) {
            // First find the errors node without parsing all the other token and throw an exception if necessary
            JsonParser pointingOnErrors = findNextObject(bufferedResponse.asParserOnFirstToken(), "errors");
            throwExceptionOnErrorEntry(pointingOnErrors);

            // Find result node and check if it's an array.
            JsonParser unbufferedResults = findNextObject(bufferedResponse.asParserOnFirstToken(), "results");
            throwExceptionOnIncorrectResultEntry(unbufferedResults);

            // Initialize columns and statistics eagerly to be at least clear that those are only the first one
            TokenBuffer resultBuffer = createTokenBuffer(unbufferedResults);
            this.columns = readColumns(resultBuffer);
            this.queryStatistics = readQueryStatistics(resultBuffer);
            this.results = new ResultNodeIterator(this.mapper, resultBuffer.asParserOnFirstToken(), flatMapData);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TokenBuffer createTokenBuffer(InputStream inputStream) throws IOException {

        // That parser is copied over as a whole to the buffer.
        // We need to parse the whole result into buffer, there's no way around it anyway.
        try (JsonParser jsonParser = JSON_FACTORY.createParser(inputStream)) {

            // First start parsing
            jsonParser.nextToken();
            // Then copy the whole thing
            return createTokenBuffer(jsonParser);
        }
    }

    private TokenBuffer createTokenBuffer(JsonParser jsonParser) throws IOException {

        TokenBuffer tokenBuffer = new TokenBuffer(mapper, true);
        tokenBuffer.copyCurrentStructure(jsonParser);

        return tokenBuffer;
    }

    private static JsonParser findNextObject(JsonParser parser, String nodeName) throws IOException {

        JsonToken jsonToken;
        while ((jsonToken = parser.nextToken()) != null) {
            parser.skipChildren();
            if (JsonToken.FIELD_NAME.equals(jsonToken) && parser.getCurrentName().equals(nodeName)) {
                parser.nextToken();
                return parser;
            }
        }
        return null;
    }

    private void throwExceptionOnErrorEntry(JsonParser pointingToErrors) throws IOException {

        if (pointingToErrors == null) {
            return;
        }

        JsonNode errorsNode = mapper.readTree(pointingToErrors);
        Optional<JsonNode> optionalErrorNode = StreamSupport.stream(errorsNode.spliterator(), false)
            .findFirst();
        if (optionalErrorNode.isPresent()) {
            JsonNode errorNode = optionalErrorNode.get();
            throw new CypherException(errorNode.findValue("code").asText(), errorNode.findValue("message").asText());
        }
    }

    private static void throwExceptionOnIncorrectResultEntry(JsonParser pointingToResults) throws IOException {

        if (pointingToResults == null) {
            throw new IOException("Response doesn't contain any results.");
        }

        if (!JsonToken.START_ARRAY.equals(pointingToResults.currentToken())) {
            throw new IOException("Current result object is not an array!");
        }
    }

    private String[] readColumns(TokenBuffer bufferedResults) throws IOException {

        JsonParser parser = bufferedResults.asParserOnFirstToken();
        parser.nextToken();
        parser = findNextObject(parser, "columns");
        if (parser == null) {
            return new String[0];
        } else {
            return mapper.readValue(parser, String[].class);
        }
    }

    private QueryStatistics readQueryStatistics(TokenBuffer bufferedResults) throws IOException {

        JsonParser parser = bufferedResults.asParserOnFirstToken();
        parser.nextToken();
        parser = findNextObject(parser, "stats");
        if (parser != null) {
            return mapper.readValue(parser, QueryStatisticsModel.class);
        }

        return null;
    }

    T nextDataRecord(String key) {
        try {
            if (results.hasNext()) {
                JsonNode dataNode = results.next();
                T t = dataNode.has(key) ? mapper.treeToValue(dataNode.get(key), resultClass) : null;
                return t;
            }
        } catch (IOException e) {
            throw new ResultProcessingException("Error processing results", e);
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
        return columns;
    }

    /**
     * Extract stats from the response if present
     *
     * @return queryStatistics or null if the response does not contain it
     */
    QueryStatistics statistics() {
        return queryStatistics;
    }

    @Override
    public void close() {
        try {
            this.results.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static class ResultNodeIterator implements Iterator<JsonNode>, AutoCloseable {

        private final ObjectMapper objectMapper;
        private final JsonParser results;
        /**
         * A flag if the the data node of one result row should be flat mapped or not.
         */
        private final boolean flatMapData;

        private Iterator<JsonNode> currentDataNodes;

        ResultNodeIterator(ObjectMapper objectMapper, JsonParser results, boolean flatMapData) {
            this.objectMapper = objectMapper;
            this.results = results;
            this.flatMapData = flatMapData;
        }

        @Override
        public boolean hasNext() {

            boolean moreDataNodes = this.currentDataNodes != null && this.currentDataNodes.hasNext();
            try {
                // This loop is necessary results of multiple statements where
                // some statements may have entries in the data node and others may not
                while (!moreDataNodes && results.nextToken() != JsonToken.END_ARRAY) {
                    JsonNode resultNode = objectMapper.readTree(results);
                    if (!flatMapData) {
                        currentDataNodes = Collections.singletonList(resultNode).iterator();
                    } else {
                        JsonNode dataArrayNode = resultNode.get("data");
                        if (dataArrayNode != null && dataArrayNode.isArray()) {
                            currentDataNodes = dataArrayNode.iterator();
                        }
                    }
                    moreDataNodes = currentDataNodes.hasNext();
                }
            } catch (IOException e) {
                moreDataNodes = false;
            }

            return moreDataNodes;
        }

        @Override
        public JsonNode next() {
            return currentDataNodes.next();
        }

        @Override
        public void close() throws IOException {
            this.results.close();
        }
    }
}
