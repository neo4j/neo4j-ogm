/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRowModel;
import org.neo4j.ogm.result.ResultRowModel;

/**
 * @author Luanne Misquitta
 */
public class JsonRowResponseTest {

    private static CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    private static HttpEntity entity = mock(HttpEntity.class);

    @Before
    public void setUpMocks() {
        when(response.getEntity()).thenReturn(entity);
    }

    @Test
    public void shouldParseColumnsInRowResponseCorrectly() throws IOException {

        when(entity.getContent()).thenReturn(rowResultsAndNoErrors());

        try (Response<DefaultRowModel> rsp = new TestRowHttpResponse()) {
            assertThat(rsp.columns().length).isEqualTo(1);
            assertThat(rsp.columns()[0]).isEqualTo("collect(p)");
        }
    }

    @Test
    public void shouldParseColumnsInRowResponseWithNoColumnsCorrectly() throws IOException {

        when(entity.getContent()).thenReturn(noRowResultsAndNoErrors());

        try (Response<DefaultRowModel> rsp = new TestRowHttpResponse()) {
            assertThat(rsp.columns().length).isEqualTo(1);
            assertThat(rsp.columns()[0]).isEqualTo("collect(p)");
        }
    }

    @Test
    public void shouldParseDataInRowResponseCorrectly() throws IOException {

        when(entity.getContent()).thenReturn(rowResultsAndNoErrors());

        try (Response<DefaultRowModel> rsp = new TestRowHttpResponse()) {
            DefaultRowModel rowModel = rsp.next();
            assertThat(rowModel).isNotNull();
            Object[] rows = rowModel.getValues();
            assertThat(rows.length).isEqualTo(1);
            List<List<Map>> data = (List<List<Map>>) rows[0];
            assertThat(data.get(0).get(0).get("name")).isEqualTo("My Test");
        }
    }

    @Test
    public void shouldParseDataInCreateRowResponseCorrectly() throws IOException {

        when(entity.getContent()).thenReturn(createRowResults());

        try (Response<DefaultRowModel> rsp = new TestRowHttpResponse()) {
            DefaultRowModel rowModel = rsp.next();
            assertThat(rowModel).isNotNull();
            Object[] rows = rowModel.getValues();
            assertThat(rows.length).isEqualTo(4);
            assertThat(rows[0]).isEqualTo(388L);
            assertThat(rows[1]).isEqualTo(527L);
            assertThat(rows[2]).isEqualTo(389L);
            assertThat(rows[3]).isEqualTo(528L);
        }
    }

    @Test
    public void shouldParseDataInCustomQueryRowResponseCorrectly() throws IOException {

        when(entity.getContent()).thenReturn(customQueryRowResults());

        try (Response<DefaultRowModel> rsp = new TestRowHttpResponse()) {
            DefaultRowModel rowModel = rsp.next();
            assertThat(rowModel).isNotNull();
            Object[] rows = rowModel.getValues();
            assertThat(rows.length).isEqualTo(3);
            Map obj1 = (Map) rows[0];
            assertThat(obj1.get("name")).isEqualTo("Betty");
            assertThat(((Map) rows[1])).isEmpty();
            assertThat((String) rows[2]).isEqualTo("Peter");
        }
    }

    private InputStream rowResultsAndNoErrors() {

        final String s = "{\"results\": [{\"columns\": [\"collect(p)\"],\"data\": [{\"row\": [[[{\"name\": \"My Test\"}]]]}]}],\"errors\": []}";

        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }

    private InputStream noRowResultsAndNoErrors() {

        final String s = "{\"results\": [{\"columns\": [\"collect(p)\"],\"data\": [{\"row\": [[]]}]}],\"errors\": []}";

        return new ByteArrayInputStream(s.getBytes(UTF_8));
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
        return new ByteArrayInputStream(s.getBytes(UTF_8));
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
        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }

    static class TestRowHttpResponse extends AbstractHttpResponse<ResultRowModel> implements Response<DefaultRowModel> {

        TestRowHttpResponse() {
            super(response, ResultRowModel.class);
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
