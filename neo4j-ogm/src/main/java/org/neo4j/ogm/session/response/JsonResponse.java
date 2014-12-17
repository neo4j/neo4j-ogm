package org.neo4j.ogm.session.response;

import java.io.InputStream;
import java.util.Scanner;

public class JsonResponse implements Neo4jResponse<String> {

    private static final String COMMA = ",";
    private static final String START_RECORD_TOKEN = "{\"";
    private static final String NEXT_RECORD_TOKEN  = COMMA + START_RECORD_TOKEN;
    private static final String ERRORS_TOKEN = "],\"errors";
    private static final String COLUMNS_TOKEN = "{\"columns";

    private final InputStream results;
    private final Scanner scanner;
    private String scanToken = null;
    private String[] columns;
    private int currentRow = -1;

    public JsonResponse(InputStream results) {
        this.results = results;
        this.scanner = new Scanner(results);
    }

    public void initialiseScan(String token) {
        this.scanToken = token;
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

                json = json.substring(0, json.indexOf(ERRORS_TOKEN));
                // todo: should check errors? they will usually not exist if we have data
            }
            String record = START_RECORD_TOKEN + scanToken + json;
            currentRow++;
            return record;

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
            this.columns = colStart.substring(colStart.indexOf("[") + 1, colStart.indexOf("]")).replaceAll("\"", "").split(",");
        }
    }

    private void parseErrors(String header) {
        int cp = header.indexOf(ERRORS_TOKEN);
        if (cp == -1) {
            throw new RuntimeException("Unexpected problem! Cypher response starts: " + header + "...");
        }
        String errStart = header.substring(cp);
        String errors = errStart.substring(errStart.indexOf("[") + 1, errStart.indexOf("]"));
        throw new RuntimeException(errors);
    }
}
