package org.neo4j.ogm.driver.http.response;

import org.neo4j.ogm.driver.impl.result.ResultProcessingException;

import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * @author vince
 */
public abstract class AbstractHttpResponse {

    private static final String COMMA = ",";
    private static final String START_RECORD_TOKEN = "{";
    private static final String NEXT_RECORD_TOKEN  = COMMA + START_RECORD_TOKEN;

    private static final String ERRORS_TOKEN = "],\"errors";
    private static final String COMMIT_ERRORS_TOKEN = "},\"errors";
    private static final String COLUMNS_TOKEN = "{\"columns";

    private final InputStream results;
    private final Scanner scanner;

    protected String[] columns;
    private int currentRow = -1;


    public AbstractHttpResponse(InputStream inputStream)  {
        this.results = inputStream;
        this.scanner = new Scanner(results, "UTF-8");
        initialise();
    }

    private void initialise() {
        this.scanner.useDelimiter(scanToken());
        // we can only auto-parse columns if the response up to the first scan token includes columns.
        // this is a hack. If we don't parse this, the user has to... TODO: Fix this.
        if (!scanToken().equals("\"results")) {
            parseColumns();
        } else {
            this.scanner.next(); // just consume up to the first token
        }
    }

    public String nextRecord() {

        try {
            String json = scanner.next();

            while (!json.endsWith(NEXT_RECORD_TOKEN)) {
                // the scan token may be embedded in the current response record, we need to keep parsing...
                try {
                    String rest = scanner.next();
                    json = json + scanToken() + rest;
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
            String record = START_RECORD_TOKEN + scanToken() + json;
            currentRow++;
            return record;

        } catch (NoSuchElementException e) {
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
            throw new RuntimeException("Unexpected problem! Cypher response starts: " + header + "...");
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

        throw new ResultProcessingException(sb.substring(cp + 2), null);
    }

    public abstract String scanToken();
}
