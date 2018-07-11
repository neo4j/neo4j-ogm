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

package org.neo4j.ogm.drivers.http.request;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * A small set of integration test introduced to ensure existing behaviour in certain error conditions and to test
 * new behaviour in situations where the opposite party does not response with valid JSON messages for example.
 *
 * @author Michael J. Simons
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpRequestTest {

    @Mock
    private CloseableHttpClient mockedHttpClient;

    @Mock
    private StatusLine mockedStatusLine;

    @Mock
    private CloseableHttpResponse mockedResponse;

    @Mock
    private HttpEntity mockedEntity;

    @Test
    public void shouldHandleErrorJsonResponseGracefully() throws IOException {
        final String failWithJsonUrl = "http://localhost/failWithJson";
        final String validJsonResponse = ""
            + "{\"errors\":["
            + " {\"message\":\"This is an error\"}"
            + ",{\"message\":\"This is another error\"}"
            + "]}";

        final HttpGet httpGet = new HttpGet(failWithJsonUrl);

        when(mockedStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_BAD_GATEWAY);
        when(mockedResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedEntity.getContent()).thenReturn(new ByteArrayInputStream(validJsonResponse.getBytes(StandardCharsets.UTF_8)));
        when(mockedResponse.getEntity()).thenReturn(mockedEntity);
        when(mockedHttpClient.execute(httpGet)).thenReturn(mockedResponse);

        try {
            HttpRequest.execute(mockedHttpClient, httpGet, null);
            fail();
        } catch(HttpRequestException e) {
            assertThat(e, isA(HttpRequestException.class));
            assertThat(e.getCause(), instanceOf(HttpResponseException.class));
            assertThat(e.getCause().getMessage(), containsString("This is an error"));
        }
    }

    @Test
    public void shouldHandleErrorNonJsonResponseGracefully() throws IOException {
        final String failWithHtmlUrl = "http://localhost/failWithHtml";
        final String htmlResponse = ""
            + "<!DOCTYPE html>"
            + "<html lang=\"en\">"
            + "<head>"
            + "<meta charset=\"utf-8\">"
            + "<title>Error</title>"
            + "</head>"
            + "<body>"
            + "<pre>Cannot POST /db/data/transaction/commit</pre>"
            + "</body>"
            + "</html> ";

        final HttpGet httpGet = new HttpGet(failWithHtmlUrl);

        when(mockedStatusLine.getStatusCode()).thenReturn(HttpStatus.SC_BAD_GATEWAY);
        when(mockedResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedEntity.getContent()).thenReturn(new ByteArrayInputStream(htmlResponse.getBytes(StandardCharsets.UTF_8)));
        when(mockedResponse.getEntity()).thenReturn(mockedEntity);
        when(mockedHttpClient.execute(httpGet)).thenReturn(mockedResponse);

        try {
            HttpRequest.execute(mockedHttpClient, httpGet, null);
            fail();
        } catch(HttpRequestException e) {
            assertThat(e, isA(HttpRequestException.class));
            assertThat(e.getCause(), instanceOf(HttpResponseException.class));
            assertThat(e.getCause().getMessage(), containsString("Could not parse the servers response as JSON"));
        }
    }
}
