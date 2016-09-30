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
import org.neo4j.ogm.model.GraphRowListModel;
import org.neo4j.ogm.model.GraphRowModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultGraphRowListModel;
import org.neo4j.ogm.result.ResultGraphRowListModel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Luanne Misquitta
 */
public class JsonGraphRowResponseTest
{

    private static CloseableHttpResponse response = mock( CloseableHttpResponse.class );
    private static HttpEntity entity = mock( HttpEntity.class );

    @Before
    public void setUpMocks()
    {
        when( response.getEntity() ).thenReturn( entity );
    }

    @Test
    public void shouldParseDataInFilterGraphResponseCorrectly() throws IOException
    {
        when(entity.getContent()).thenReturn(filterQueryGraphRowResponse());

        try ( Response< GraphRowListModel > rsp = new TestGraphRowHttpResponse(  ) )
        {
            GraphRowListModel graphRowListModel = rsp.next();
            TestCase.assertNotNull( graphRowListModel );

            List< GraphRowModel > graphRowModels = graphRowListModel.model();
            TestCase.assertEquals( 8, graphRowModels.size() );
            GraphRowModel model = graphRowModels.get( 0 );
            GraphModel graph = model.getGraph();
            TestCase.assertEquals( Long.valueOf( 26 ), graph.getNodes().iterator().next().getId() );
            TestCase.assertEquals( 0, graph.getRelationships().size() );
            Object[] rows = model.getRow();
            TestCase.assertEquals( 2, rows.length );
            Map row1 = (Map) ( (List) rows[0] ).get( 0 );
            TestCase.assertEquals( "GraphAware", row1.get( "name" ) );
            TestCase.assertEquals( 26, rows[1] );
        }
    }

    private InputStream filterQueryGraphRowResponse()
    {
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
        try {
            return new ByteArrayInputStream(s.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            fail("UTF-8 encoding not supported on this platform");
            throw new IllegalStateException("cannot be reached");
        }
    }

    static class TestGraphRowHttpResponse extends AbstractHttpResponse< ResultGraphRowListModel > implements Response< GraphRowListModel >
    {

        public TestGraphRowHttpResponse()
        {
            super( response, ResultGraphRowListModel.class );
        }

        @Override
        public GraphRowListModel next()
        {
            ResultGraphRowListModel graphRowModel = nextDataRecord( "data" );

            if ( graphRowModel != null )
            {
                DefaultGraphRowListModel graphRowListModel = new DefaultGraphRowListModel();
                for ( GraphRowModel model : graphRowModel.getData() )
                {
                    graphRowListModel.add( model );
                }
                return graphRowListModel;
            }
            return null;
        }

        @Override
        public void close()
        {
            //Nothing to do, the response has been closed already
        }
    }
}
