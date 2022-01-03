/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.drivers.http.response;

import static java.nio.charset.StandardCharsets.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Node;
import org.neo4j.ogm.response.Response;

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

        try (Response<GraphModel> rsp = new GraphModelResponse(response)) {
            assertThat(rsp.columns().length).isEqualTo(1);
            assertThat(rsp.columns()[0]).isEqualTo("_0");
        }
    }

    @Test
    public void shouldParseColumnsInGraphResponseWithNoColumnsCorrectly() throws IOException {
        when(entity.getContent()).thenReturn(noGraphResultsAndNoErrors());

        try (Response<GraphModel> rsp = new GraphModelResponse(response)) {
            assertThat(rsp.columns().length).isEqualTo(1);
            assertThat(rsp.columns()[0]).isEqualTo("_0");
        }
    }

    @Test
    public void shouldParseDataInLoadByIdsGraphResponseCorrectly() throws IOException {

        when(entity.getContent()).thenReturn(loadByIdsGraphResults());

        try (Response<GraphModel> rsp = new GraphModelResponse(response)) {
            GraphModel graphModel = rsp.next();
            assertThat(graphModel).isNotNull();
            Collection<Node> nodes = graphModel.getNodes();
            assertThat(nodes).hasSize(1);
            assertThat(nodes.iterator().next().getPropertyList().get(0).getValue()).isEqualTo("adam");
            assertThat(graphModel.getRelationships()).isEmpty();

            graphModel = rsp.next();
            assertThat(graphModel).isNotNull();
            nodes = graphModel.getNodes();
            assertThat(nodes).hasSize(2);
            Iterator<Node> nodeIterator = nodes.iterator();
            nodeIterator.next(); //skip adam
            assertThat(nodeIterator.next().getPropertyList().get(0).getValue()).isEqualTo("GraphAware");
            assertThat(graphModel.getRelationships()).hasSize(1);
            assertThat(graphModel.getRelationships().iterator().next().getType()).isEqualTo("EMPLOYED_BY");

            for (int i = 0; i < 4; i++) {
                assertThat(rsp.next()).isNotNull();
            }
            assertThat(rsp.next()).isNull();
        }
    }

    private InputStream graphResultsAndNoErrors() {

        final String s = "{\n" +
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
            "                \"labels\": [\n" +
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

        return new ByteArrayInputStream(s.getBytes(UTF_8));
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

        return new ByteArrayInputStream(s.getBytes(UTF_8));
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
            "                \"labels\": [\n" +
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
            "                \"labels\": [\n" +
            "                  \"User\"\n" +
            "                ],\n" +
            "                \"properties\": {\n" +
            "                  \"firstName\": \"adam\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"id\": \"26\",\n" +
            "                \"labels\": [\n" +
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
            "                \"labels\": [\n" +
            "                  \"User\"\n" +
            "                ],\n" +
            "                \"properties\": {\n" +
            "                  \"firstName\": \"adam\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"id\": \"347\",\n" +
            "                \"labels\": [\n" +
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
            "                \"labels\": [\n" +
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
            "                \"labels\": [\n" +
            "                  \"Customer\"\n" +
            "                ],\n" +
            "                \"properties\": {\n" +
            "                  \"name\": \"GraphAware\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"id\": \"344\",\n" +
            "                \"labels\": [\n" +
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
            "                \"labels\": [\n" +
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
            "                \"labels\": [\n" +
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
        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }
}
