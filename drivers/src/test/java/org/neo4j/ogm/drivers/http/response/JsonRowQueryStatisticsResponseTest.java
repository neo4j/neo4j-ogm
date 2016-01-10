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

import static junit.framework.TestCase.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.neo4j.ogm.json.ObjectMapperFactory;
import org.neo4j.ogm.model.QueryStatistics;
import org.neo4j.ogm.model.RowStatisticsModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRowStatisticsModel;
import org.neo4j.ogm.result.ResultRowModel;

/**
 * @author Luanne Misquitta
 */
public class JsonRowQueryStatisticsResponseTest {

    @Test
    public void shouldParseDataInUpdateQueryWithNodesReturnedAndStatisticsCorrectly() {
        try (Response<RowStatisticsModel> rsp = new TestRowStatisticsHttpResponse(updateQueryWithNodesReturnedAndStatisticsResponse())) {
            RowStatisticsModel rowStatisticsModel = rsp.next();
            assertNotNull(rowStatisticsModel);

            Collection<Object[]> rows = rowStatisticsModel.getRows();
            assertEquals(2,rows.size());

            Iterator<Object[]> rowsIt = rows.iterator();

            Object[] row1 = rowsIt.next();
            Map row1Data = (Map)row1[0];
            assertEquals(30,row1Data.get("age"));
            assertEquals("8",row1Data.get("number"));
            assertEquals("fake 8",row1Data.get("title"));

            Object[] row2 = rowsIt.next();
            Map row2Data = (Map)row2[0];
            assertEquals(30,row2Data.get("age"));
            assertEquals("GraphAware",row2Data.get("name"));

            QueryStatistics stats = rowStatisticsModel.getStats();
            assertNotNull(stats);
            assertTrue(stats.containsUpdates());
            assertEquals(0,stats.getNodesCreated());
            assertEquals(2,stats.getPropertiesSet());
        }
    }

    @Test
    public void shouldParseDataInUpdateQueryWithPropertyReturnedAndStatisticsCorrectly() {
        try (Response<RowStatisticsModel> rsp = new TestRowStatisticsHttpResponse(updateQueryWithPropertyReturnedAndStatistics())) {
            RowStatisticsModel rowStatisticsModel = rsp.next();
            assertNotNull(rowStatisticsModel);

            Collection<Object[]> rows = rowStatisticsModel.getRows();
            assertEquals(2,rows.size());

            Iterator<Object[]> rowsIt = rows.iterator();

            Object[] row1 = rowsIt.next();
            assertEquals(30,row1[0]);

            Object[] row2 = rowsIt.next();
            assertEquals(30,row2[0]);

            QueryStatistics stats = rowStatisticsModel.getStats();
            assertNotNull(stats);
            assertTrue(stats.containsUpdates());
            assertEquals(0,stats.getNodesCreated());
            assertEquals(2,stats.getPropertiesSet());
        }
    }

    @Test
    public void shouldParseDataInUpdateQueryNoResultsAndStatisticsResponseCorrectly() {
        try (Response<RowStatisticsModel> rsp = new TestRowStatisticsHttpResponse(updateQueryNoResultsAndStatisticsResponse())) {
            RowStatisticsModel rowStatisticsModel = rsp.next();
            assertNotNull(rowStatisticsModel);

            Collection<Object[]> rows = rowStatisticsModel.getRows();
            assertEquals(0,rows.size());

            QueryStatistics stats = rowStatisticsModel.getStats();
            assertNotNull(stats);
            assertTrue(stats.containsUpdates());
            assertEquals(0,stats.getNodesCreated());
            assertEquals(2,stats.getPropertiesSet());
        }

    }

    private InputStream updateQueryWithNodesReturnedAndStatisticsResponse() {
        final String s="{\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"columns\": [\n" +
                "        \"a\"\n" +
                "      ],\n" +
                "      \"data\": [\n" +
                "        {\n" +
                "          \"row\": [\n" +
                "            {\n" +
                "              \"age\": 30,\n" +
                "              \"number\": \"8\",\n" +
                "              \"title\": \"fake 8\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"row\": [\n" +
                "            {\n" +
                "              \"age\": 30,\n" +
                "              \"name\": \"GraphAware\"\n" +
                "            }\n" +
                "          ]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"stats\": {\n" +
                "        \"contains_updates\": true,\n" +
                "        \"nodes_created\": 0,\n" +
                "        \"nodes_deleted\": 0,\n" +
                "        \"properties_set\": 2,\n" +
                "        \"relationships_created\": 0,\n" +
                "        \"relationship_deleted\": 0,\n" +
                "        \"labels_added\": 0,\n" +
                "        \"labels_removed\": 0,\n" +
                "        \"indexes_added\": 0,\n" +
                "        \"indexes_removed\": 0,\n" +
                "        \"constraints_added\": 0,\n" +
                "        \"constraints_removed\": 0\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"errors\": []\n" +
                "}";
        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream updateQueryWithPropertyReturnedAndStatistics() {
        final String s="{\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"columns\": [\n" +
                "        \"a.age\"\n" +
                "      ],\n" +
                "      \"data\": [\n" +
                "        {\n" +
                "          \"row\": [\n" +
                "            30\n" +
                "          ]\n" +
                "        },\n" +
                "        {\n" +
                "          \"row\": [\n" +
                "            30\n" +
                "          ]\n" +
                "        }\n" +
                "      ],\n" +
                "      \"stats\": {\n" +
                "        \"contains_updates\": true,\n" +
                "        \"nodes_created\": 0,\n" +
                "        \"nodes_deleted\": 0,\n" +
                "        \"properties_set\": 2,\n" +
                "        \"relationships_created\": 0,\n" +
                "        \"relationship_deleted\": 0,\n" +
                "        \"labels_added\": 0,\n" +
                "        \"labels_removed\": 0,\n" +
                "        \"indexes_added\": 0,\n" +
                "        \"indexes_removed\": 0,\n" +
                "        \"constraints_added\": 0,\n" +
                "        \"constraints_removed\": 0\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"errors\": []\n" +
                "}";
        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream updateQueryNoResultsAndStatisticsResponse() {
        final String s="{\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"columns\": [],\n" +
                "      \"data\": [],\n" +
                "      \"stats\": {\n" +
                "        \"contains_updates\": true,\n" +
                "        \"nodes_created\": 0,\n" +
                "        \"nodes_deleted\": 0,\n" +
                "        \"properties_set\": 2,\n" +
                "        \"relationships_created\": 0,\n" +
                "        \"relationship_deleted\": 0,\n" +
                "        \"labels_added\": 0,\n" +
                "        \"labels_removed\": 0,\n" +
                "        \"indexes_added\": 0,\n" +
                "        \"indexes_removed\": 0,\n" +
                "        \"constraints_added\": 0,\n" +
                "        \"constraints_removed\": 0\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"errors\": []\n" +
                "}";
        return new ByteArrayInputStream(s.getBytes());
    }

    static class TestRowStatisticsHttpResponse extends AbstractHttpResponse<ResultRowModel> implements Response<RowStatisticsModel> {
        protected static final ObjectMapper mapper = ObjectMapperFactory.objectMapper();

        public TestRowStatisticsHttpResponse(InputStream inputStream) {
            super(inputStream, ResultRowModel.class);
        }

        @Override
        public RowStatisticsModel next() {
            DefaultRowStatisticsModel rowQueryStatisticsResult = new DefaultRowStatisticsModel();
            ResultRowModel rowModel = nextDataRecord("row");
            while (rowModel != null) {
                rowQueryStatisticsResult.addRow(rowModel.queryResults());
                rowModel = nextDataRecord("row");
            }
            rowQueryStatisticsResult.setStats(statistics());
            return rowQueryStatisticsResult;
        }


        @Override
        public void close() {
            //Nothing to do, the response has been closed already
        }

    }
}
