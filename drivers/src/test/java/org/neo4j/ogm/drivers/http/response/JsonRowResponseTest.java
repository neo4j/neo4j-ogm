/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

import static junit.framework.TestCase.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRowModel;
import org.neo4j.ogm.result.ResultRowModel;

/**
 * @author Luanne Misquitta
 */
public class JsonRowResponseTest {

    @Test
    public void shouldParseColumnsInRowResponseCorrectly() {
        try (Response<DefaultRowModel> rsp = new TestRowHttpResponse((rowResultsAndNoErrors()))) {
            assertEquals(1, rsp.columns().length);
            assertEquals("collect(p)",rsp.columns()[0]);
        }
    }

    @Test
    public void shouldParseColumnsInRowResponseWithNoColumnsCorrectly() {
        try (Response<DefaultRowModel> rsp = new TestRowHttpResponse((noRowResultsAndNoErrors()))) {
            assertEquals(1, rsp.columns().length);
            assertEquals("collect(p)",rsp.columns()[0]);

        }
    }

    @Test
    public void shouldParseDataInRowResponseCorrectly() {
        try (Response<DefaultRowModel> rsp = new TestRowHttpResponse((rowResultsAndNoErrors()))) {
            DefaultRowModel rowModel = rsp.next();
            assertNotNull(rowModel);
            Object[] rows = rowModel.getValues();
            assertEquals(1,rows.length);
            List<List<Map>> data = (List<List<Map>>) rows[0];
            assertEquals("My Test",data.get(0).get(0).get("name"));
        }
    }

    @Test
    public void shouldParseDataInCreateRowResponseCorrectly() {
        try (Response<DefaultRowModel> rsp = new TestRowHttpResponse((createRowResults()))) {
            DefaultRowModel rowModel = rsp.next();
            assertNotNull(rowModel);
            Object[] rows = rowModel.getValues();
            assertEquals(4,rows.length);
            assertEquals(388,rows[0]);
            assertEquals(527,rows[1]);
            assertEquals(389,rows[2]);
            assertEquals(528,rows[3]);
        }
    }

    @Test
    public void shouldParseDataInCustomQueryRowResponseCorrectly() {
        try (Response<DefaultRowModel> rsp = new TestRowHttpResponse((customQueryRowResults()))) {
            DefaultRowModel rowModel = rsp.next();
            assertNotNull(rowModel);
            Object[] rows = rowModel.getValues();
            assertEquals(3,rows.length);
            Map obj1 = (Map)rows[0];
            assertEquals("Betty", obj1.get("name"));
            assertEquals(0,((Map)rows[1]).size());
            assertEquals("Peter",(String)rows[2]);
        }
    }


    private InputStream rowResultsAndNoErrors() {

        final String s= "{\"results\": [{\"columns\": [\"collect(p)\"],\"data\": [{\"row\": [[[{\"name\": \"My Test\"}]]]}]}],\"errors\": []}";

        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream noRowResultsAndNoErrors() {

        final String s = "{\"results\": [{\"columns\": [\"collect(p)\"],\"data\": [{\"row\": [[]]}]}],\"errors\": []}";

        return new ByteArrayInputStream(s.getBytes());
    }


    private InputStream createRowResults() {
        final String s = "{\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"columns\": [\n" +
                "        \"_0\",\n" +
                "        \"_1\",\n" +
                "        \"_2\",\n" +
                "        \"_3\"\n" +
                "      ],\n" +
                "      \"data\": [\n" +
                "        {\n" +
                "          \"row\": [\n" +
                "            388,\n" +
                "            527,\n" +
                "            389,\n" +
                "            528\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"errors\": []\n" +
                "}";
        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream customQueryRowResults() {
        final String s = "{\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"columns\": [\n" +
                "        \"n\",\n" +
                "        \"r\",\n" +
                "        \"m.name\"\n" +
                "      ],\n" +
                "      \"data\": [\n" +
                "        {\n" +
                "          \"row\": [\n" +
                "            {\n" +
                "              \"name\": \"Betty\"\n" +
                "            },\n" +
                "            {},\n" +
                "            \"Peter\"\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"row\": [\n" +
                "            {\n" +
                "              \"name\": \"Betty\"\n" +
                "            },\n" +
                "            {},\n" +
                "            \"Patrick\"\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"row\": [\n" +
                "            {\n" +
                "              \"name\": \"Betty\"\n" +
                "            },\n" +
                "            {},\n" +
                "            \"Priscilla\"\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"errors\": []\n" +
                "}";
        return new ByteArrayInputStream(s.getBytes());
    }

    static class TestRowHttpResponse extends AbstractHttpResponse<ResultRowModel> implements Response<DefaultRowModel> {

        public TestRowHttpResponse(InputStream inputStream) {
            super(inputStream, ResultRowModel.class);
        }

        @Override
        public DefaultRowModel next() {
            ResultRowModel rowModel = nextDataRecord("row");

            if (rowModel != null) {
                return new DefaultRowModel(rowModel.queryResults(), columns());
            }
            return null;
        }

        @Override
        public void close() {
            //Nothing to do, the response has been closed already
        }

    }
}
