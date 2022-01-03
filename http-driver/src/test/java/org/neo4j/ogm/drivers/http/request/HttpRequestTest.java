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
package org.neo4j.ogm.drivers.http.request;

import static org.apache.http.HttpHeaders.*;
import static org.apache.http.entity.ContentType.*;
import static org.assertj.core.api.Assertions.*;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.junit.Test;
import org.neo4j.ogm.exception.ConnectionException;

import com.github.paweladamski.httpclientmock.HttpClientMock;

/**
 * A small set of integration test introduced to ensure existing behaviour in certain error conditions and to test
 * new behaviour in situations where the opposite party does not response with valid JSON messages for example.
 *
 * @author Michael J. Simons
 */
public class HttpRequestTest {

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

        assertThatExceptionOfType(ConnectionException.class)
            .isThrownBy(() -> HttpRequest.execute(httpClientMock, new HttpGet(failWithJsonUrl), null))
            .withRootCauseInstanceOf(HttpResponseException.class)
            .withStackTraceContaining("This is an error");
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

        assertThatExceptionOfType(ConnectionException.class)
            .isThrownBy(() -> HttpRequest.execute(httpClientMock, new HttpGet(failWithHtmlUrl), null))
            .withRootCauseInstanceOf(HttpResponseException.class)
            .withStackTraceContaining("Could not parse the servers response as JSON");
    }
}
