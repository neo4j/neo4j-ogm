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
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.response.Response;

/**
 * @author Luanne Misquitta
 */
public class JsonGraphRowResponseTest {

    private static CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    private static HttpEntity entity = mock(HttpEntity.class);

    @Before
    public void setUpMocks() {
        when(response.getEntity()).thenReturn(entity);
    }

    @Test
    public void shouldParseDataInFilterGraphResponseCorrectly() throws IOException {
        when(entity.getContent()).thenReturn(filterQueryGraphRowResponse());

        try (Response<GraphRowListModel> rsp = new GraphRowsModelResponse(response)) {
            GraphRowListModel graphRowListModel = rsp.next();
            assertThat(graphRowListModel).isNotNull();

            List<GraphRowModel> graphRowModels = graphRowListModel.model();
            assertThat(graphRowModels).hasSize(8);
            GraphRowModel model = graphRowModels.get(0);
            GraphModel graph = model.getGraph();
            assertThat(graph.getNodes().iterator().next().getId()).isEqualTo(Long.valueOf(26));
            assertThat(graph.getRelationships()).isEmpty();
            Object[] rows = model.getRow();
            assertThat(rows.length).isEqualTo(2);
            Map row1 = (Map) ((List) rows[0]).get(0);
            assertThat(row1.get("name")).isEqualTo("GraphAware");
            assertThat(rows[1]).isEqualTo(26L);
        }
    }

    private InputStream filterQueryGraphRowResponse() {
        final String s = "{\n" +
            "  \"results\": [\n" +
            "    {\n" +
            "      \"columns\": [\n" +
            "        \"p\",\n" +
            "        \"ID(n)\"\n" +
            "      ],\n" +
            "      \"data\": [\n" +
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
            "              }\n" +
            "            ],\n" +
            "            \"relationships\": []\n" +
            "          },\n" +
            "          \"row\": [\n" +
            "            [\n" +
            "              {\n" +
            "                \"name\": \"GraphAware\"\n" +
            "              }\n" +
            "            ],\n" +
            "            26\n" +
            "          ]\n" +
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
            "          },\n" +
            "          \"row\": [\n" +
            "            [\n" +
            "              {\n" +
            "                \"name\": \"GraphAware\"\n" +
            "              },\n" +
            "              {},\n" +
            "              {\n" +
            "                \"firstName\": \"vince\"\n" +
            "              }\n" +
            "            ],\n" +
            "            26\n" +
            "          ]\n" +
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
            "          },\n" +
            "          \"row\": [\n" +
            "            [\n" +
            "              {\n" +
            "                \"name\": \"GraphAware\"\n" +
            "              },\n" +
            "              {},\n" +
            "              {\n" +
            "                \"firstName\": \"adam\"\n" +
            "              }\n" +
            "            ],\n" +
            "            26\n" +
            "          ]\n" +
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
            "                \"id\": \"346\",\n" +
            "                \"labels\": [\n" +
            "                  \"Issue\"\n" +
            "                ],\n" +
            "                \"properties\": {\n" +
            "                  \"title\": \"fake 1\",\n" +
            "                  \"number\": \"1\",\n" +
            "                  \"title\": \"fake 1\"\n" +
            "                }\n" +
            "              }\n" +
            "            ],\n" +
            "            \"relationships\": [\n" +
            "              {\n" +
            "                \"id\": \"20\",\n" +
            "                \"type\": \"LOGGED_BY\",\n" +
            "                \"startNode\": \"346\",\n" +
            "                \"endNode\": \"26\",\n" +
            "                \"properties\": {}\n" +
            "              }\n" +
            "            ]\n" +
            "          },\n" +
            "          \"row\": [\n" +
            "            [\n" +
            "              {\n" +
            "                \"name\": \"GraphAware\"\n" +
            "              },\n" +
            "              {},\n" +
            "              {\n" +
            "                \"title\": \"fake 1\",\n" +
            "                \"number\": \"1\",\n" +
            "                \"title\": \"fake 1\"\n" +
            "              }\n" +
            "            ],\n" +
            "            26\n" +
            "          ]\n" +
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
            "                \"id\": \"501\",\n" +
            "                \"type\": \"LOGGED_BY\",\n" +
            "                \"startNode\": \"347\",\n" +
            "                \"endNode\": \"26\",\n" +
            "                \"properties\": {}\n" +
            "              }\n" +
            "            ]\n" +
            "          },\n" +
            "          \"row\": [\n" +
            "            [\n" +
            "              {\n" +
            "                \"name\": \"GraphAware\"\n" +
            "              },\n" +
            "              {},\n" +
            "              {\n" +
            "                \"title\": \"fake 7\",\n" +
            "                \"number\": \"7\",\n" +
            "                \"title\": \"fake 7\"\n" +
            "              }\n" +
            "            ],\n" +
            "            26\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"graph\": {\n" +
            "            \"nodes\": [\n" +
            "              {\n" +
            "                \"id\": \"27\",\n" +
            "                \"labels\": [\n" +
            "                  \"Company\"\n" +
            "                ],\n" +
            "                \"properties\": {\n" +
            "                  \"name\": \"Acme\"\n" +
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
            "                \"id\": \"502\",\n" +
            "                \"type\": \"LOCATED_AT\",\n" +
            "                \"startNode\": \"26\",\n" +
            "                \"endNode\": \"27\",\n" +
            "                \"properties\": {}\n" +
            "              }\n" +
            "            ]\n" +
            "          },\n" +
            "          \"row\": [\n" +
            "            [\n" +
            "              {\n" +
            "                \"name\": \"GraphAware\"\n" +
            "              },\n" +
            "              {},\n" +
            "              {\n" +
            "                \"name\": \"Acme\"\n" +
            "              }\n" +
            "            ],\n" +
            "            26\n" +
            "          ]\n" +
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
            "                \"id\": \"28\",\n" +
            "                \"labels\": [\n" +
            "                  \"Company\"\n" +
            "                ],\n" +
            "                \"properties\": {\n" +
            "                  \"name\": \"CodeIsUs\"\n" +
            "                }\n" +
            "              }\n" +
            "            ],\n" +
            "            \"relationships\": [\n" +
            "              {\n" +
            "                \"id\": \"503\",\n" +
            "                \"type\": \"LOCATED_AT\",\n" +
            "                \"startNode\": \"26\",\n" +
            "                \"endNode\": \"28\",\n" +
            "                \"properties\": {}\n" +
            "              }\n" +
            "            ]\n" +
            "          },\n" +
            "          \"row\": [\n" +
            "            [\n" +
            "              {\n" +
            "                \"name\": \"GraphAware\"\n" +
            "              },\n" +
            "              {},\n" +
            "              {\n" +
            "                \"name\": \"CodeIsUs\"\n" +
            "              }\n" +
            "            ],\n" +
            "            26\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"graph\": {\n" +
            "            \"nodes\": [\n" +
            "              {\n" +
            "                \"id\": \"0\",\n" +
            "                \"labels\": [\n" +
            "                  \"Issue\"\n" +
            "                ],\n" +
            "                \"properties\": {\n" +
            "                  \"number\": \"8\",\n" +
            "                  \"title\": \"fake 8\"\n" +
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
            "                \"id\": \"520\",\n" +
            "                \"type\": \"LOGGED_BY\",\n" +
            "                \"startNode\": \"0\",\n" +
            "                \"endNode\": \"26\",\n" +
            "                \"properties\": {}\n" +
            "              }\n" +
            "            ]\n" +
            "          },\n" +
            "          \"row\": [\n" +
            "            [\n" +
            "              {\n" +
            "                \"name\": \"GraphAware\"\n" +
            "              },\n" +
            "              {},\n" +
            "              {\n" +
            "                \"number\": \"8\",\n" +
            "                \"title\": \"fake 8\"\n" +
            "              }\n" +
            "            ],\n" +
            "            26\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"errors\": []\n" +
            "}";
        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }
}
