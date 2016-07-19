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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.drivers.http.response.AbstractHttpResponse;
import org.neo4j.ogm.drivers.http.response.RestModelAdapter;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRestModel;
import org.neo4j.ogm.response.model.NodeModel;
import org.neo4j.ogm.result.ResultRestModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Luanne Misquitta
 */
public class JsonRestResponseTest {

    private static CloseableHttpResponse response = mock( CloseableHttpResponse.class );
    private static HttpEntity entity = mock( HttpEntity.class );

    @Before
    public void setUpMocks()
    {
        when( response.getEntity() ).thenReturn( entity );
    }

    @Test
    public void shouldParseColumnsInRowResponseCorrectly() throws IOException {

        when(entity.getContent()).thenReturn(rowResultsAndNoErrors());

        try (Response<DefaultRestModel> rsp = new TestRestHttpResponse()) {
            Assert.assertEquals( 3, rsp.columns().length );
            Assert.assertEquals( "count", rsp.columns()[0] );
        }
    }

  @Test
    public void shouldParseColumnsInRowResponseWithNoColumnsCorrectly() throws IOException {

      when(entity.getContent()).thenReturn(noRowResultsAndNoErrors());

      try (Response<DefaultRestModel> rsp = new TestRestHttpResponse()) {
          Assert.assertEquals( 3, rsp.columns().length );
            Assert.assertEquals( "director", rsp.columns()[1] );

        }
    }

     @Test
    public void shouldParseDataInRowResponseCorrectly() throws IOException {

         when(entity.getContent()).thenReturn(rowResultsAndNoErrors());

         try (Response<DefaultRestModel> rsp = new TestRestHttpResponse()) {
            DefaultRestModel restModel = rsp.next();
            TestCase.assertNotNull( restModel );
            Map<String,Object> rows = restModel.getRow();
            Assert.assertEquals( 3, rows.entrySet().size() );

            Assert.assertEquals( 1, rows.get( "count" ) );
            NodeModel data = (NodeModel) rows.get("director");
            Assert.assertEquals( 1931, data.property( "born" ) );
            data = (NodeModel) rows.get("movie");
            Assert.assertEquals( "The Birdcage", data.property( "title" ) );
            Assert.assertEquals( 395L, data.getId().longValue() );

            restModel = rsp.next();
            rows = restModel.getRow();
            Assert.assertEquals( 3, rows.entrySet().size() );
            Assert.assertEquals( 1, rows.get( "count" ) );
            data = (NodeModel) rows.get("director");
            Assert.assertEquals( 1931, data.property( "born" ) );
            data = (NodeModel) rows.get("movie");
            Assert.assertEquals( 2007, data.property( "released" ) );
        }
    }

    private InputStream rowResultsAndNoErrors() {

        final String s= "{\"results\":[{\"columns\":[\"count\",\"director\",\"movie\"],\"data\":[{\"rest\":[1,{\"outgoing_relationships\":\"http://localhost:7474/db/data/node/396/relationships/out\",\"addedLabels\":\"http://localhost:7474/db/data/node/396/addedLabels\",\"all_typed_relationships\":\"http://localhost:7474/db/data/node/396/relationships/all/{-list|&|types}\",\"traverse\":\"http://localhost:7474/db/data/node/396/traverse/{returnType}\",\"self\":\"http://localhost:7474/db/data/node/396\",\"property\":\"http://localhost:7474/db/data/node/396/properties/{key}\",\"outgoing_typed_relationships\":\"http://localhost:7474/db/data/node/396/relationships/out/{-list|&|types}\",\"properties\":\"http://localhost:7474/db/data/node/396/properties\",\"incoming_relationships\":\"http://localhost:7474/db/data/node/396/relationships/in\",\"create_relationship\":\"http://localhost:7474/db/data/node/396/relationships\",\"paged_traverse\":\"http://localhost:7474/db/data/node/396/paged/traverse/{returnType}{?pageSize,leaseTime}\",\"all_relationships\":\"http://localhost:7474/db/data/node/396/relationships/all\",\"incoming_typed_relationships\":\"http://localhost:7474/db/data/node/396/relationships/in/{-list|&|types}\",\"metadata\":{\"id\":396,\"addedLabels\":[\"Person\"]},\"data\":{\"born\":1931,\"name\":\"Mike Nichols\"}},{\"outgoing_relationships\":\"http://localhost:7474/db/data/node/395/relationships/out\",\"addedLabels\":\"http://localhost:7474/db/data/node/395/addedLabels\",\"all_typed_relationships\":\"http://localhost:7474/db/data/node/395/relationships/all/{-list|&|types}\",\"traverse\":\"http://localhost:7474/db/data/node/395/traverse/{returnType}\",\"self\":\"http://localhost:7474/db/data/node/395\",\"property\":\"http://localhost:7474/db/data/node/395/properties/{key}\",\"outgoing_typed_relationships\":\"http://localhost:7474/db/data/node/395/relationships/out/{-list|&|types}\",\"properties\":\"http://localhost:7474/db/data/node/395/properties\",\"incoming_relationships\":\"http://localhost:7474/db/data/node/395/relationships/in\",\"create_relationship\":\"http://localhost:7474/db/data/node/395/relationships\",\"paged_traverse\":\"http://localhost:7474/db/data/node/395/paged/traverse/{returnType}{?pageSize,leaseTime}\",\"all_relationships\":\"http://localhost:7474/db/data/node/395/relationships/all\",\"incoming_typed_relationships\":\"http://localhost:7474/db/data/node/395/relationships/in/{-list|&|types}\",\"metadata\":{\"id\":395,\"addedLabels\":[\"Movie\"]},\"data\":{\"released\":1996,\"title\":\"The Birdcage\",\"tagline\":\"Come as you are\"}}]},{\"rest\":[1,{\"outgoing_relationships\":\"http://localhost:7474/db/data/node/396/relationships/out\",\"addedLabels\":\"http://localhost:7474/db/data/node/396/addedLabels\",\"all_typed_relationships\":\"http://localhost:7474/db/data/node/396/relationships/all/{-list|&|types}\",\"traverse\":\"http://localhost:7474/db/data/node/396/traverse/{returnType}\",\"self\":\"http://localhost:7474/db/data/node/396\",\"property\":\"http://localhost:7474/db/data/node/396/properties/{key}\",\"outgoing_typed_relationships\":\"http://localhost:7474/db/data/node/396/relationships/out/{-list|&|types}\",\"properties\":\"http://localhost:7474/db/data/node/396/properties\",\"incoming_relationships\":\"http://localhost:7474/db/data/node/396/relationships/in\",\"create_relationship\":\"http://localhost:7474/db/data/node/396/relationships\",\"paged_traverse\":\"http://localhost:7474/db/data/node/396/paged/traverse/{returnType}{?pageSize,leaseTime}\",\"all_relationships\":\"http://localhost:7474/db/data/node/396/relationships/all\",\"incoming_typed_relationships\":\"http://localhost:7474/db/data/node/396/relationships/in/{-list|&|types}\",\"metadata\":{\"id\":396,\"addedLabels\":[\"Person\"]},\"data\":{\"born\":1931,\"name\":\"Mike Nichols\"}},{\"outgoing_relationships\":\"http://localhost:7474/db/data/node/457/relationships/out\",\"addedLabels\":\"http://localhost:7474/db/data/node/457/addedLabels\",\"all_typed_relationships\":\"http://localhost:7474/db/data/node/457/relationships/all/{-list|&|types}\",\"traverse\":\"http://localhost:7474/db/data/node/457/traverse/{returnType}\",\"self\":\"http://localhost:7474/db/data/node/457\",\"property\":\"http://localhost:7474/db/data/node/457/properties/{key}\",\"outgoing_typed_relationships\":\"http://localhost:7474/db/data/node/457/relationships/out/{-list|&|types}\",\"properties\":\"http://localhost:7474/db/data/node/457/properties\",\"incoming_relationships\":\"http://localhost:7474/db/data/node/457/relationships/in\",\"create_relationship\":\"http://localhost:7474/db/data/node/457/relationships\",\"paged_traverse\":\"http://localhost:7474/db/data/node/457/paged/traverse/{returnType}{?pageSize,leaseTime}\",\"all_relationships\":\"http://localhost:7474/db/data/node/457/relationships/all\",\"incoming_typed_relationships\":\"http://localhost:7474/db/data/node/457/relationships/in/{-list|&|types}\",\"metadata\":{\"id\":457,\"addedLabels\":[\"Movie\"]},\"data\":{\"released\":2007,\"title\":\"Charlie Wilson's War\",\"tagline\":\"A stiff drink. A little mascara. A lot of nerve. Who said they couldn't bring down the Soviet empire.\"}}]}]}],\"errors\":[]}";

        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream noRowResultsAndNoErrors() {

        final String s = "{\"results\":[{\"columns\":[\"count\",\"director\",\"movie\"],\"data\":[]}],\"errors\":[]}";

        return new ByteArrayInputStream(s.getBytes());
    }



    static class TestRestHttpResponse extends AbstractHttpResponse<ResultRestModel> implements Response<DefaultRestModel> {

        public TestRestHttpResponse() {
            super(response, ResultRestModel.class);
        }
        private RestModelAdapter restModelAdapter = new RestModelAdapter();

        @Override
        public DefaultRestModel next() {
            restModelAdapter.setColumns(columns());
            DefaultRestModel defaultRestModel = new DefaultRestModel(buildModel());
            defaultRestModel.setStats(statistics());
            return defaultRestModel;
        }

        @Override
        public void close() {
            //Nothing to do, the response has been closed already
        }



        private Map<String,Object> buildModel() {
            ResultRestModel result = nextDataRecord("rest");
            Map<String,Object> row = new LinkedHashMap<>();
            if (result != null) {
                row = restModelAdapter.adapt(result.queryResults());
            }

            return row;
        }

    }
}
