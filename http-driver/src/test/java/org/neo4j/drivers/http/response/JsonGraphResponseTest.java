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

package org.neo4j.drivers.http.response;

import junit.framework.TestCase;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.drivers.http.response.AbstractHttpResponse;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.result.ResultGraphModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Luanne Misquitta
 */
public class JsonGraphResponseTest {

    private static CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    private static HttpEntity entity = mock(HttpEntity.class);

    @Before
    public void setUpMocks() {
        when(response.getEntity()).thenReturn(entity);
    }
    @Test
    public void shouldParseColumnsInGraphResponseCorrectly() throws IOException {

        when(entity.getContent()).thenReturn(graphResultsAndNoErrors());

        try (Response<GraphModel> rsp = new TestGraphHttpResponse()) {
            TestCase.assertEquals( 1, rsp.columns().length );
            TestCase.assertEquals( "_0", rsp.columns()[0] );
        }
    }

    @Test
    public void shouldParseColumnsInGraphResponseWithNoColumnsCorrectly() throws IOException
    {
        when(entity.getContent()).thenReturn(noGraphResultsAndNoErrors());

        try (Response<GraphModel> rsp = new TestGraphHttpResponse()) {
            TestCase.assertEquals( 1, rsp.columns().length );
            TestCase.assertEquals( "_0", rsp.columns()[0] );
        }
    }

    @Test
    public void shouldParseDataInLoadByIdsGraphResponseCorrectly() throws IOException {

        when(entity.getContent()).thenReturn(loadByIdsGraphResults());

        try (Response<GraphModel> rsp = new TestGraphHttpResponse()) {
            GraphModel graphModel = rsp.next();
            TestCase.assertNotNull( graphModel );
            Set<Node> nodes = graphModel.getNodes();
            TestCase.assertEquals( 1, nodes.size() );
            TestCase.assertEquals( "adam", nodes.iterator().next().getPropertyList().get( 0 ).getValue() );
            TestCase.assertEquals( 0, graphModel.getRelationships().size() );

            graphModel = rsp.next();
            TestCase.assertNotNull( graphModel );
            nodes = graphModel.getNodes();
            TestCase.assertEquals( 2, nodes.size() );
            Iterator<Node> nodeIterator = nodes.iterator();
            nodeIterator.next(); //skip adam
            TestCase.assertEquals( "GraphAware", nodeIterator.next().getPropertyList().get( 0 ).getValue() );
            TestCase.assertEquals( 1, graphModel.getRelationships().size() );
            TestCase.assertEquals( "EMPLOYED_BY", graphModel.getRelationships().iterator().next().getType() );

            for (int i=0;i<4;i++) {
                TestCase.assertNotNull( rsp.next() );
            }
            TestCase.assertNull( rsp.next() );
        }
    }

    private InputStream graphResultsAndNoErrors() {

        final String s= "{\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"columns\": [\n" +
                "        \"_0\"\n" +
                "      ],\n" +
                "      \"data\": [\n" +
                "        {\n" +
                "          \"graph\": {\n" +
                "            \"nodes\": [\n" +
                "              {\n" +
                "                \"id\": \"381\",\n" +
                "                \"addedLabels\": [\n" +
                "                  \"School\"\n" +
                "                ],\n" +
                "                \"properties\": {}\n" +
                "              }\n" +
                "            ],\n" +
                "            \"relationships\": []\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"errors\": []\n" +
                "}";

        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream noGraphResultsAndNoErrors() {

        final String s = "{\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"columns\": [\n" +
                "        \"_0\"\n" +
                "      ],\n" +
                "      \"data\": []\n" +
                "    }\n" +
                "  ],\n" +
                "  \"errors\": []\n" +
                "}";

        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream loadByIdsGraphResults() {
        final String s = "{\n" +
                "  \"results\": [\n" +
                "    {\n" +
                "      \"columns\": [\n" +
                "        \"p\"\n" +
                "      ],\n" +
                "      \"data\": [\n" +
                "        {\n" +
                "          \"graph\": {\n" +
                "            \"nodes\": [\n" +
                "              {\n" +
                "                \"id\": \"343\",\n" +
                "                \"addedLabels\": [\n" +
                "                  \"User\"\n" +
                "                ],\n" +
                "                \"properties\": {\n" +
                "                  \"firstName\": \"adam\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"relationships\": []\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"graph\": {\n" +
                "            \"nodes\": [\n" +
                "              {\n" +
                "                \"id\": \"343\",\n" +
                "                \"addedLabels\": [\n" +
                "                  \"User\"\n" +
                "                ],\n" +
                "                \"properties\": {\n" +
                "                  \"firstName\": \"adam\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\": \"26\",\n" +
                "                \"addedLabels\": [\n" +
                "                  \"Customer\"\n" +
                "                ],\n" +
                "                \"properties\": {\n" +
                "                  \"name\": \"GraphAware\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"relationships\": [\n" +
                "              {\n" +
                "                \"id\": \"18\",\n" +
                "                \"type\": \"EMPLOYED_BY\",\n" +
                "                \"startNode\": \"343\",\n" +
                "                \"endNode\": \"26\",\n" +
                "                \"properties\": {}\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"graph\": {\n" +
                "            \"nodes\": [\n" +
                "              {\n" +
                "                \"id\": \"343\",\n" +
                "                \"addedLabels\": [\n" +
                "                  \"User\"\n" +
                "                ],\n" +
                "                \"properties\": {\n" +
                "                  \"firstName\": \"adam\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\": \"347\",\n" +
                "                \"addedLabels\": [\n" +
                "                  \"Issue\"\n" +
                "                ],\n" +
                "                \"properties\": {\n" +
                "                  \"title\": \"fake 7\",\n" +
                "                  \"number\": \"7\",\n" +
                "                  \"title\": \"fake 7\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"relationships\": [\n" +
                "              {\n" +
                "                \"id\": \"506\",\n" +
                "                \"type\": \"ASSIGNED_TO\",\n" +
                "                \"startNode\": \"347\",\n" +
                "                \"endNode\": \"343\",\n" +
                "                \"properties\": {}\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"graph\": {\n" +
                "            \"nodes\": [\n" +
                "              {\n" +
                "                \"id\": \"344\",\n" +
                "                \"addedLabels\": [\n" +
                "                  \"User\"\n" +
                "                ],\n" +
                "                \"properties\": {\n" +
                "                  \"firstName\": \"vince\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"relationships\": []\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"graph\": {\n" +
                "            \"nodes\": [\n" +
                "              {\n" +
                "                \"id\": \"26\",\n" +
                "                \"addedLabels\": [\n" +
                "                  \"Customer\"\n" +
                "                ],\n" +
                "                \"properties\": {\n" +
                "                  \"name\": \"GraphAware\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\": \"344\",\n" +
                "                \"addedLabels\": [\n" +
                "                  \"User\"\n" +
                "                ],\n" +
                "                \"properties\": {\n" +
                "                  \"firstName\": \"vince\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"relationships\": [\n" +
                "              {\n" +
                "                \"id\": \"19\",\n" +
                "                \"type\": \"EMPLOYED_BY\",\n" +
                "                \"startNode\": \"344\",\n" +
                "                \"endNode\": \"26\",\n" +
                "                \"properties\": {}\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        },\n" +
                "        {\n" +
                "          \"graph\": {\n" +
                "            \"nodes\": [\n" +
                "              {\n" +
                "                \"id\": \"346\",\n" +
                "                \"addedLabels\": [\n" +
                "                  \"Issue\"\n" +
                "                ],\n" +
                "                \"properties\": {\n" +
                "                  \"title\": \"fake 1\",\n" +
                "                  \"number\": \"1\",\n" +
                "                  \"title\": \"fake 1\"\n" +
                "                }\n" +
                "              },\n" +
                "              {\n" +
                "                \"id\": \"344\",\n" +
                "                \"addedLabels\": [\n" +
                "                  \"User\"\n" +
                "                ],\n" +
                "                \"properties\": {\n" +
                "                  \"firstName\": \"vince\"\n" +
                "                }\n" +
                "              }\n" +
                "            ],\n" +
                "            \"relationships\": [\n" +
                "              {\n" +
                "                \"id\": \"509\",\n" +
                "                \"type\": \"CREATED\",\n" +
                "                \"startNode\": \"344\",\n" +
                "                \"endNode\": \"346\",\n" +
                "                \"properties\": {}\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"errors\": []\n" +
                "}";
        return new ByteArrayInputStream(s.getBytes());

    }

    static class TestGraphHttpResponse extends AbstractHttpResponse<ResultGraphModel> implements Response<GraphModel> {

        public TestGraphHttpResponse() {
            super(response, ResultGraphModel.class);
        }

        @Override
        public GraphModel next() {
            ResultGraphModel graphModel = nextDataRecord("graph");

            if (graphModel != null) {
                return graphModel.queryResults();
            }
            return null;
        }

        @Override
        public void close() {
            //Nothing to do, the response has been closed already
        }

    }
//
}
