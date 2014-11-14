package org.neo4j.ogm.session.response;

import java.io.InputStream;
import java.util.Scanner;

public class JsonResponseHandler implements Neo4jResponseHandler<String> {

    private static final String COMMA = ",";
    private static final String START_RECORD_TOKEN = "{\"";
    private static final String NEXT_RECORD_TOKEN  = COMMA + START_RECORD_TOKEN;
    private static final String ERRORS_TOKEN = "],\"errors";

    private final InputStream results;
    private final Scanner scanner;
    private String scanToken = null;

    public JsonResponseHandler(InputStream results) {
        this.results = results;
        this.scanner = new Scanner(results);
    }

    public void setScanToken(String token) {
        this.scanToken = token;
        this.scanner.useDelimiter(scanToken);
        // TODO: this currently assumes only ONE data[] element in the response stream.
        this.scanner.next(); // consume the header
    }

    // todo: throw a CypherException if there are errors in the request.
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
                json = json.substring(0, json.indexOf(ERRORS_TOKEN));
                // todo: should check errors!
            }
            return START_RECORD_TOKEN + scanToken + json;

        } catch (Exception e) {
            return null;
        }
    }

    public void close() {
        try {
            results.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
