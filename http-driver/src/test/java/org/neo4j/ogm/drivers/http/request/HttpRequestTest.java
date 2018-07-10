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

import static org.apache.http.HttpHeaders.*;
import static org.apache.http.entity.ContentType.*;
import static org.hamcrest.Matchers.*;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.paweladamski.httpclientmock.HttpClientMock;

/**
 * A small set of integration test introduced to ensure existing behaviour in certain error conditions and to test
 * new behaviour in situations where the opposite party does not response with valid JSON messages for example.
 *
 * @author Michael J. Simons
 */
public class HttpRequestTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldHandleErrorJsonResponseGracefully() {
        final String failWithJsonUrl = "http://localhost/failWithJson";
        final String validJsonResponse = ""
            + "{\"errors\":["
            + " {\"message\":\"This is an error\"}"
            + ",{\"message\":\"This is another error\"}"
            + "]}";

        final HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet(failWithJsonUrl)
            .doReturn(validJsonResponse)
            .withStatus(HttpStatus.SC_BAD_GATEWAY)
            .withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType());

        expectedException.expect(HttpRequestException.class);
        expectedException.expectCause(isA(HttpResponseException.class));
        expectedException.expectMessage(containsString("This is an error"));

        HttpRequest.execute(httpClientMock, new HttpGet(failWithJsonUrl), null);
    }

    @Test
    public void shouldHandleErrorNonJsonResponseGracefully() {
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

        final HttpClientMock httpClientMock = new HttpClientMock();
        httpClientMock.onGet(failWithHtmlUrl)
            .doReturn(htmlResponse)
            .withStatus(HttpStatus.SC_BAD_GATEWAY)
            .withHeader(CONTENT_TYPE, TEXT_HTML.getMimeType());

        expectedException.expect(HttpRequestException.class);
        expectedException.expectCause(isA(HttpResponseException.class));
        expectedException.expectMessage(containsString("Could not parse the servers response as JSON"));

        HttpRequest.execute(httpClientMock, new HttpGet(failWithHtmlUrl), null);
    }
}
