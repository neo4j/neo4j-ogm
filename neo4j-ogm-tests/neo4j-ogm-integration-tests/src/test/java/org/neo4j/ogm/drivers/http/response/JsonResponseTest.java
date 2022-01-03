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
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.model.RowModel;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRowModel;

/**
 * @author Vince Bickers
 */
public class JsonResponseTest {

    private static CloseableHttpResponse response = mock(CloseableHttpResponse.class);
    private static HttpEntity entity = mock(HttpEntity.class);

    @Before
    public void setUpMocks() {
        when(response.getEntity()).thenReturn(entity);
    }

    @Test(expected = CypherException.class)
    public void shouldHandleNoResultsAndErrors() throws IOException {
        when(entity.getContent()).thenReturn(noResultsAndErrors());

        try (Response<RowModel> rsp = new RowModelResponse(response)) {
            parseResponse(rsp);
        }
    }

    @Test(expected = CypherException.class)
    public void shouldHandleResultsAndErrors() throws IOException {

        when(entity.getContent()).thenReturn(resultsAndErrors());

        try (Response<RowModel> rsp = new RowModelResponse(response)) {
            parseResponse(rsp);
        }
    }

    @Test
    public void shouldHandleNoResultsAndNoErrors() throws IOException {

        when(entity.getContent()).thenReturn(noRowResultsAndNoErrors());

        try (Response<RowModel> rsp = new RowModelResponse(response)) {
            parseResponse(rsp);
        }
    }

    @Test
    public void shouldHandleResultsAndNoErrors() throws IOException {

        when(entity.getContent()).thenReturn(rowResultsAndNoErrors());

        try (Response<RowModel> rsp = new RowModelResponse(response)) {
            parseResponse(rsp);
        }
    }

    private void parseResponse(Response<RowModel> rsp) {

        //CHECKSTYLE:OFF
        while (rsp.next() != null) {
        }
        //CHECKSTYLE:ON
    }

    private InputStream resultsAndErrors() {
        String s = "{\n" +
            "  \"results\": [\n" +
            "    {\n" +
            "      \"columns\": [\n" +
            "        \"ID(_0)\"\n" +
            "      ],\n" +
            "      \"data\": [\n" +
            "        {\n" +
            "          \"row\": [\n" +
            "            381\n" +
            "          ]\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"errors\": [\n" +
            "    {\n" +
            "      \"code\": \"Neo.DatabaseError.Transaction.CouldNotCommit\",\n" +
            "      \"message\": \"org.neo4j.kernel.api.exceptions.TransactionFailureException: Node record Node[381,used=false,rel=526,prop=-1,labels=Inline(0x0:[]),light] still has relationships\",\n"
            +
            "      \"stackTrace\": \"java.lang.RuntimeException: org.neo4j.kernel.api.exceptions.TransactionFailureException: Node record Node[381,used=false,rel=526,prop=-1,labels=Inline(0x0:[]),light] still has relationships\\n\\tat org.neo4j.server.rest.transactional.TransitionalTxManagementKernelTransaction.commit(TransitionalTxManagementKernelTransaction.java:87)\\n\\tat org.neo4j.server.rest.transactional.TransactionHandle.closeContextAndCollectErrors(TransactionHandle.java:279)\\n\\tat org.neo4j.server.rest.transactional.TransactionHandle.commit(TransactionHandle.java:148)\\n\\tat org.neo4j.server.rest.web.TransactionalService$2.write(TransactionalService.java:211)\\n\\tat com.sun.jersey.core.impl.provider.entity.StreamingOutputProvider.writeTo(StreamingOutputProvider.java:71)\\n\\tat com.sun.jersey.core.impl.provider.entity.StreamingOutputProvider.writeTo(StreamingOutputProvider.java:57)\\n\\tat com.sun.jersey.spi.container.ContainerResponse.write(ContainerResponse.java:302)\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1510)\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)\\n\\tat com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)\\n\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:790)\\n\\tat org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:800)\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1669)\\n\\tat org.neo4j.server.rest.dbms.AuthorizationFilter.doFilter(AuthorizationFilter.java:116)\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1652)\\n\\tat org.neo4j.server.rest.web.CollectUserAgentFilter.doFilter(CollectUserAgentFilter.java:69)\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1652)\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:585)\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:221)\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1125)\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:515)\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:185)\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1059)\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)\\n\\tat org.eclipse.jetty.server.handler.HandlerList.handle(HandlerList.java:52)\\n\\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:97)\\n\\tat org.eclipse.jetty.server.Server.handle(Server.java:497)\\n\\tat org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:310)\\n\\tat org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:248)\\n\\tat org.eclipse.jetty.io.AbstractConnection$2.run(AbstractConnection.java:540)\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:620)\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:540)\\n\\tat java.lang.Thread.run(Thread.java:724)\\nCaused by: org.neo4j.kernel.api.exceptions.TransactionFailureException: Node record Node[381,used=false,rel=526,prop=-1,labels=Inline(0x0:[]),light] still has relationships\\n\\tat org.neo4j.kernel.impl.transaction.state.IntegrityValidator.validateNodeRecord(IntegrityValidator.java:52)\\n\\tat org.neo4j.kernel.impl.transaction.state.TransactionRecordState.extractCommands(TransactionRecordState.java:170)\\n\\tat org.neo4j.kernel.impl.api.KernelTransactionImplementation.commit(KernelTransactionImplementation.java:537)\\n\\tat org.neo4j.kernel.impl.api.KernelTransactionImplementation.close(KernelTransactionImplementation.java:456)\\n\\tat org.neo4j.server.rest.transactional.TransitionalTxManagementKernelTransaction.commit(TransitionalTxManagementKernelTransaction.java:83)\\n\\t... 35 more\\n\"\n"
            +
            "    }\n" +
            "  ]\n" +
            "}";
        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }

    private InputStream noResultsAndErrors() {
        String s = "{\n" +
            "  \"results\": [],\n" +
            "  \"errors\": [\n" +
            "    {\n" +
            "      \"code\": \"Neo.ClientError.Statement.InvalidSyntax\",\n" +
            "      \"message\": \"Invalid input 'T': expected 'a/A' (line 1, column 4 (offset: 3))\\n\\\"CRETE (_0:`School`:`DomainObject`{_0_props}) RETURN id(_0) AS _0 \\\"\\n    ^\"\n"
            +
            "    }\n" +
            "  ]\n" +
            "}";

        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }

    private InputStream rowResultsAndNoErrors() {

        final String s = "{\"results\": [{\"columns\": [\"collect(p)\"],\"data\": [{\"row\": [[[{\"name\": \"My Test\"}]]]}]}],\"errors\": []}";

        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }

    private InputStream noRowResultsAndNoErrors() {

        final String s = "{\"results\": [{\"columns\": [\"collect(p)\"],\"data\": [{\"row\": [[]]}]}],\"errors\": []}";

        return new ByteArrayInputStream(s.getBytes(UTF_8));
    }
}
