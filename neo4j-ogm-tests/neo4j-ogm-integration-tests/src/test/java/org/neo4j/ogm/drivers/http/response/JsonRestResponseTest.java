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
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.model.RestModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.NodeModel;

/**
 * @author Luanne Misquitta
 */
public class JsonRestResponseTest {

    private static CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    private static HttpEntity entity = mock(HttpEntity.class);

    @Before
    public void setUpMocks() {
        when(response.getEntity()).thenReturn(entity);
    }

    @Test
    public void shouldParseColumnsInRowResponseCorrectly() throws IOException {

        when(entity.getContent()).thenReturn(rowResultsAndNoErrors());

        try (Response<RestModel> rsp = new RestModelResponse(response)) {
            assertThat(rsp.columns().length).isEqualTo(3);
            assertThat(rsp.columns()[0]).isEqualTo("count");
        }
    }

    @Test
    public void shouldParseColumnsInRowResponseWithNoColumnsCorrectly() throws IOException {

        when(entity.getContent()).thenReturn(noRowResultsAndNoErrors());

        try (Response<RestModel> rsp = new RestModelResponse(response)) {
            assertThat(rsp.columns().length).isEqualTo(3);
            assertThat(rsp.columns()[1]).isEqualTo("director");
        }
    }

    @Test
    public void shouldParseDataInRowResponseCorrectly() throws IOException {

        when(entity.getContent()).thenReturn(rowResultsAndNoErrors());

        try (Response<RestModel> rsp = new RestModelResponse(response)) {
            RestModel restModel = rsp.next();
            assertThat(restModel).isNotNull();
            Map<String, Object> rows = restModel.getRow();
            assertThat(rows.entrySet()).hasSize(3);

            assertThat(rows.get("count")).isEqualTo(1L);
            NodeModel data = (NodeModel) rows.get("director");
            assertThat(data.property("born")).isEqualTo(1931L);
            data = (NodeModel) rows.get("movie");
            assertThat(data.property("title")).isEqualTo("The Birdcage");
            assertThat(data.getId().longValue()).isEqualTo(395L);

            restModel = rsp.next();
            rows = restModel.getRow();
            assertThat(rows.entrySet()).hasSize(3);
            assertThat(rows.get("count")).isEqualTo(1L);
            data = (NodeModel) rows.get("director");
            assertThat(data.property("born")).isEqualTo(1931L);
            data = (NodeModel) rows.get("movie");
            assertThat(data.property("released")).isEqualTo(2007L);
        }
    }

    private InputStream rowResultsAndNoErrors() {

        final String s = "{\"results\":[{\"columns\":[\"count\",\"director\",\"movie\"],\"data\":[{\"rest\":[1,{\"outgoing_relationships\":\"http://localhost:7474/db/data/node/396/relationships/out\",\"labels\":\"http://localhost:7474/db/data/node/396/labels\",\"all_typed_relationships\":\"http://localhost:7474/db/data/node/396/relationships/all/{-list|&|types}\",\"traverse\":\"http://localhost:7474/db/data/node/396/traverse/{returnType}\",\"self\":\"http://localhost:7474/db/data/node/396\",\"property\":\"http://localhost:7474/db/data/node/396/properties/{key}\",\"outgoing_typed_relationships\":\"http://localhost:7474/db/data/node/396/relationships/out/{-list|&|types}\",\"properties\":\"http://localhost:7474/db/data/node/396/properties\",\"incoming_relationships\":\"http://localhost:7474/db/data/node/396/relationships/in\",\"create_relationship\":\"http://localhost:7474/db/data/node/396/relationships\",\"paged_traverse\":\"http://localhost:7474/db/data/node/396/paged/traverse/{returnType}{?pageSize,leaseTime}\",\"all_relationships\":\"http://localhost:7474/db/data/node/396/relationships/all\",\"incoming_typed_relationships\":\"http://localhost:7474/db/data/node/396/relationships/in/{-list|&|types}\",\"metadata\":{\"id\":396,\"labels\":[\"Person\"]},\"data\":{\"born\":1931,\"name\":\"Mike Nichols\"}},{\"outgoing_relationships\":\"http://localhost:7474/db/data/node/395/relationships/out\",\"labels\":\"http://localhost:7474/db/data/node/395/labels\",\"all_typed_relationships\":\"http://localhost:7474/db/data/node/395/relationships/all/{-list|&|types}\",\"traverse\":\"http://localhost:7474/db/data/node/395/traverse/{returnType}\",\"self\":\"http://localhost:7474/db/data/node/395\",\"property\":\"http://localhost:7474/db/data/node/395/properties/{key}\",\"outgoing_typed_relationships\":\"http://localhost:7474/db/data/node/395/relationships/out/{-list|&|types}\",\"properties\":\"http://localhost:7474/db/data/node/395/properties\",\"incoming_relationships\":\"http://localhost:7474/db/data/node/395/relationships/in\",\"create_relationship\":\"http://localhost:7474/db/data/node/395/relationships\",\"paged_traverse\":\"http://localhost:7474/db/data/node/395/paged/traverse/{returnType}{?pageSize,leaseTime}\",\"all_relationships\":\"http://localhost:7474/db/data/node/395/relationships/all\",\"incoming_typed_relationships\":\"http://localhost:7474/db/data/node/395/relationships/in/{-list|&|types}\",\"metadata\":{\"id\":395,\"labels\":[\"Movie\"]},\"data\":{\"released\":1996,\"title\":\"The Birdcage\",\"tagline\":\"Come as you are\"}}]},{\"rest\":[1,{\"outgoing_relationships\":\"http://localhost:7474/db/data/node/396/relationships/out\",\"labels\":\"http://localhost:7474/db/data/node/396/labels\",\"all_typed_relationships\":\"http://localhost:7474/db/data/node/396/relationships/all/{-list|&|types}\",\"traverse\":\"http://localhost:7474/db/data/node/396/traverse/{returnType}\",\"self\":\"http://localhost:7474/db/data/node/396\",\"property\":\"http://localhost:7474/db/data/node/396/properties/{key}\",\"outgoing_typed_relationships\":\"http://localhost:7474/db/data/node/396/relationships/out/{-list|&|types}\",\"properties\":\"http://localhost:7474/db/data/node/396/properties\",\"incoming_relationships\":\"http://localhost:7474/db/data/node/396/relationships/in\",\"create_relationship\":\"http://localhost:7474/db/data/node/396/relationships\",\"paged_traverse\":\"http://localhost:7474/db/data/node/396/paged/traverse/{returnType}{?pageSize,leaseTime}\",\"all_relationships\":\"http://localhost:7474/db/data/node/396/relationships/all\",\"incoming_typed_relationships\":\"http://localhost:7474/db/data/node/396/relationships/in/{-list|&|types}\",\"metadata\":{\"id\":396,\"labels\":[\"Person\"]},\"data\":{\"born\":1931,\"name\":\"Mike Nichols\"}},{\"outgoing_relationships\":\"http://localhost:7474/db/data/node/457/relationships/out\",\"labels\":\"http://localhost:7474/db/data/node/457/labels\",\"all_typed_relationships\":\"http://localhost:7474/db/data/node/457/relationships/all/{-list|&|types}\",\"traverse\":\"http://localhost:7474/db/data/node/457/traverse/{returnType}\",\"self\":\"http://localhost:7474/db/data/node/457\",\"property\":\"http://localhost:7474/db/data/node/457/properties/{key}\",\"outgoing_typed_relationships\":\"http://localhost:7474/db/data/node/457/relationships/out/{-list|&|types}\",\"properties\":\"http://localhost:7474/db/data/node/457/properties\",\"incoming_relationships\":\"http://localhost:7474/db/data/node/457/relationships/in\",\"create_relationship\":\"http://localhost:7474/db/data/node/457/relationships\",\"paged_traverse\":\"http://localhost:7474/db/data/node/457/paged/traverse/{returnType}{?pageSize,leaseTime}\",\"all_relationships\":\"http://localhost:7474/db/data/node/457/relationships/all\",\"incoming_typed_relationships\":\"http://localhost:7474/db/data/node/457/relationships/in/{-list|&|types}\",\"metadata\":{\"id\":457,\"labels\":[\"Movie\"]},\"data\":{\"released\":2007,\"title\":\"Charlie Wilson's War\",\"tagline\":\"A stiff drink. A little mascara. A lot of nerve. Who said they couldn't bring down the Soviet empire.\"}}]}]}],\"errors\":[]}";

        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }

    private InputStream noRowResultsAndNoErrors() {

        final String s = "{\"results\":[{\"columns\":[\"count\",\"director\",\"movie\"],\"data\":[]}],\"errors\":[]}";

        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }
}
