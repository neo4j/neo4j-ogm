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
package org.neo4j.ogm.unit.response;

import org.junit.Test;
import org.neo4j.ogm.session.response.JsonResponse;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.result.ResultProcessingException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author vince
 */
public class JsonResponseTest {


    @Test(expected = ResultProcessingException.class)
    public void shouldHandleNoResultsAndErrors() {
        try( JsonResponse rsp = new JsonResponse(noResultsAndErrors()) ) {
            parseResponse(rsp);
        }
    }

    @Test(expected = ResultProcessingException.class)
    public void shouldHandleResultsAndErrors() {
        try( JsonResponse rsp = new JsonResponse(resultsAndErrors()) ) {
            parseResponse(rsp);
        }
    }

    @Test
    public void shouldHandleNoResultsAndNoErrors() {
        try( JsonResponse rsp = new JsonResponse(noResultsAndNoErrors()) ) {
            parseResponse(rsp);
        }
    }

    @Test
    public void shouldHandleResultsAndNoErrors() {
        try( JsonResponse rsp = new JsonResponse(resultsAndNoErrors()) ) {
            parseResponse(rsp);
        }
    }

    private void parseResponse(JsonResponse rsp) {
        rsp.expect(Neo4jResponse.ResponseRecord.ROW);
        while (rsp.next() != null);
    }

    private InputStream resultsAndErrors() {
        String s = "{" +
                "\"results\": [{\"columns\": [\"_0\"],\"data\": [{\"row\": [0]}]}]," +
                "\"errors\": [{\"code\": \"Neo.DatabaseError.Transaction.CouldNotCommit\"," +
                "\"message\": \"org.neo4j.kernel.api.exceptions.TransactionFailureException: \"," +
                "\"stackTrace\": \"java.lang.RuntimeException: org.neo4j.kernel.api.exceptions.TransactionFailureException: \\\tat" +
                "...}]}";

        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream noResultsAndErrors() {
        String s = "{" +
                "\"results\": [{\"columns\": [\"_0\"],\"data\": [{\"row\": []," +
                "\"errors\": [{\"code\": \"Neo.DatabaseError.Statement.ExecutionFailure\"," +
                "\"message\": \"Could not create token\"," +
                "\"stackTrace\": \"org.neo4j.graphdb.TransactionFailureException: Could not create token\\\tat org.neo4j.kernel.impl.core.TokenHolder.getOrCreateId(TokenHolder.java:121)\\\tat" +
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

}
