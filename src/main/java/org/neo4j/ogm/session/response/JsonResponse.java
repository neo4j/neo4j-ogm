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

package org.neo4j.ogm.session.response;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.neo4j.ogm.session.result.CypherException;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * @author Vince Bickers
 */
public class JsonResponse implements Neo4jResponse<String> {

    private static final String COMMA = ",";
    private static final String START_RECORD_TOKEN = "{";
    private static final String NEXT_RECORD_TOKEN  = COMMA + START_RECORD_TOKEN;

    private static final String ERRORS_TOKEN = "],\"errors";
    private static final String COMMIT_ERRORS_TOKEN = "},\"errors";
    private static final String COLUMNS_TOKEN = "{\"columns";

    private static final String GRAPH_TOKEN = "\"graph";
    private static final String ROW_TOKEN = "\"row";
    private static final String RESULTS_TOKEN = "\"results";
    private static final String STATS_TOKEN = "\"stats";

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonResponse.class);

    private final InputStream results;
    private final Scanner scanner;
    private final CloseableHttpResponse response;
    private String scanToken = null;
    private String[] columns;
    private int currentRow = -1;


    public JsonResponse(CloseableHttpResponse response) {
        try {
            this.response = response;
            this.results = response.getEntity().getContent();
            this.scanner = new Scanner(results, "UTF-8");
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    public JsonResponse(InputStream inputStream)  {
        this.response = null;
        this.results = inputStream;
        this.scanner = new Scanner(results, "UTF-8");
    }

    public void initialiseScan(ResponseRecord record) {
        this.scanToken = extractToken(record);
        this.scanner.useDelimiter(scanToken);
        // TODO: this currently assumes only ONE data[] element in the response stream.
        parseColumns();
    }

    public String next() {
        try {
            String json = scanner.next();

            while (!json.endsWith(NEXT_RECORD_TOKEN)) {
                // the scan token may be embedded in the current response record, we need to keep parsing...
                try {
                    String rest = scanner.next();
                    json = json + scanToken + rest;
                } catch (Exception e) {
                    break;
                }
            }

            // will match all records except last in response
            if (json.endsWith(NEXT_RECORD_TOKEN)) {
                json = json.substring(0, json.length() - NEXT_RECORD_TOKEN.length());
            } else if (json.contains(ERRORS_TOKEN)) {

                int errorsPosition = json.indexOf(ERRORS_TOKEN);
                if (json.substring(errorsPosition).contains("[]")) {
                    json = json.substring(0, errorsPosition);
                } else {
                    parseErrors(json);
                }
            }
            String record = START_RECORD_TOKEN + scanToken + json;
            currentRow++;
            return record;

        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void close() {
        try {
            results.close();
            if (response != null) {
                LOGGER.debug("Closing HttpResponse");
                response.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String[] columns() {
        return this.columns;
    }

    @Override
    public int rowId() {
        return currentRow;
    }

    private void parseColumns() {
        String header = this.scanner.next(); // consume the header and return the columns array to the caller

        int cp = header.indexOf(COLUMNS_TOKEN);
        if (cp == -1) {
            parseErrors(header);
        } else {
            String colStart = header.substring(cp);
            this.columns = colStart.substring(colStart.indexOf("[") + 1, colStart.indexOf("]")).replace("\"", "").split(",");
        }
    }

    private void parseErrors(String header) {
        int cp = header.indexOf(ERRORS_TOKEN);
        if (cp == -1) {
            cp = header.indexOf(COMMIT_ERRORS_TOKEN);
        }
        if (cp == -1) {
            throw new ResultProcessingException("Unexpected problem! Cypher response starts: " + header + "...", null);
        }
        if (header.indexOf("code") > 0 && header.indexOf("message") > 0) {
            String code = header.substring(header.indexOf("code") + 7, header.indexOf("message")-3);
            String message = "";
            if (header.lastIndexOf("}]}") > 0) {
                message = header.substring(header.indexOf("message") + 10,header.lastIndexOf("}]}")-1);
            }
            throw new CypherException("Error executing Cypher statement", null, code, message);
        }
        StringBuilder sb = new StringBuilder(header);
        String response;
        try {
            while ((response = scanner.next()) != null) {
                sb.append(response);
            }
        } catch (Exception e) {
            scanner.close();
        }
        throw new ResultProcessingException(sb.substring( cp + 2 ), null);
    }

    private String extractToken(ResponseRecord format) {

        switch (format) {
            case GRAPH:
                return GRAPH_TOKEN;
            case ROW:
                return ROW_TOKEN;
            case RESULTS:
                return RESULTS_TOKEN;
            case STATS:
                return STATS_TOKEN;
            default:
                throw new RuntimeException("Unhandled response format: " + format);
        }
    }
}
