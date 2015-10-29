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
package org.neo4j.ogm.drivers.http.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRowModel;
import org.neo4j.ogm.result.ResultRowModel;
import org.neo4j.ogm.exception.ResultProcessingException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author vince
 */
public class JsonResponseTest {


    @Test(expected = ResultProcessingException.class)
    public void shouldHandleNoResultsAndErrors() {
        try( Response<DefaultRowModel> rsp = new TestRowHttpResponse(noResultsAndErrors()) ) {
            parseResponse(rsp);
        }
    }

    @Test(expected = ResultProcessingException.class)
    public void shouldHandleResultsAndErrors() {
        try( Response<DefaultRowModel> rsp = new TestRowHttpResponse(resultsAndErrors()) ) {
            parseResponse(rsp);
        }
    }

    @Test
    public void shouldHandleNoResultsAndNoErrors() {
        try( Response<DefaultRowModel> rsp = new TestRowHttpResponse(noResultsAndNoErrors()) ) {
            parseResponse(rsp);
        }
    }

    @Test
    public void shouldHandleResultsAndNoErrors() {
        try( Response<DefaultRowModel> rsp = new TestRowHttpResponse(resultsAndNoErrors()) ) {
            parseResponse(rsp);
        }
    }

    private void parseResponse(Response<DefaultRowModel> rsp) {
        //noinspection StatementWithEmptyBody
        while (rsp.next() != null);
    }

    private InputStream resultsAndErrors() {
        String s = "{" +
                "\"results\": [{\"columns\": [\"_0\"],\"data\": [{\"row\": [0]}]}]," +
                "\"errors\": [{\"code\": \"Neo.DatabaseError.Transaction.CouldNotCommit\"," +
                "\"message\": \"org.neo4j.kernel.exceptions.TransactionFailureException: \"," +
                "\"stackTrace\": \"java.lang.RuntimeException: org.neo4j.kernel.exceptions.TransactionFailureException: \\\tat" +
                "...}]}";

        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream noResultsAndErrors() {
        String s = "{" +
                "\"results\": [{\"columns\": [\"_0\"],\"data\": [{\"row\": []," +
                "\"errors\": [{\"code\": \"Neo.DatabaseError.Statement.ExecutionFailure\"," +
                "\"message\": \"Could not create token\"," +
                "\"stackTrace\": \"org.neo4j.graphdb.TransactionFailureException: Could not create token\\\tat org.neo4j.kernel.impl.TokenHolder.getOrCreateId(TokenHolder.java:121)\\\tat" +
                "...}]}";

        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream resultsAndNoErrors() {

        final String s= "{\"results\": [{\"columns\": [\"collect(p)\"],\"data\": [{\"row\": [[[{\"name\": \"My Test\"}]]]}]}],\"errors\": []}";

        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream noResultsAndNoErrors() {

        final String s = "{\"results\": [{\"columns\": [\"collect(p)\"],\"data\": [{\"row\": [[]]}]}],\"errors\": []}";

        return new ByteArrayInputStream(s.getBytes());
    }

    static class TestRowHttpResponse extends AbstractHttpResponse implements Response<DefaultRowModel> {

        private static final ObjectMapper mapper = new ObjectMapper();

        public TestRowHttpResponse(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public String scanToken() {
            return "\"row";
        }

        @Override
        public DefaultRowModel next() {

            String json = super.nextRecord();

            if (json != null) {
                try {
                    return new DefaultRowModel(mapper.readValue(json, ResultRowModel.class).model(), columns());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                return null;
            }
        }

    }
}
