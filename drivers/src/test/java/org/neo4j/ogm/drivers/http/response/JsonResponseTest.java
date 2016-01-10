/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 *
 */
package org.neo4j.ogm.drivers.http.response;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.response.model.DefaultRowModel;
import org.neo4j.ogm.result.ResultRowModel;

/**
 * @author vince
 */
public class JsonResponseTest {


    @Test(expected = ResultProcessingException.class)
    public void shouldHandleNoResultsAndErrors() {
        try( Response<DefaultRowModel> rsp = new TestRowHttpResponse(noResultsAndErrors()) ) {
            parseResponse(rsp);
        }
    }

    @Test(expected = ResultProcessingException.class)
    public void shouldHandleResultsAndErrors() {
        try( Response<DefaultRowModel> rsp = new TestRowHttpResponse(resultsAndErrors()) ) {
            parseResponse(rsp);
        }
    }

    @Test
    public void shouldHandleNoResultsAndNoErrors() {
        try( Response<DefaultRowModel> rsp = new TestRowHttpResponse(noRowResultsAndNoErrors()) ) {
            parseResponse(rsp);
        }
    }

    @Test
    public void shouldHandleResultsAndNoErrors() {
        try( Response<DefaultRowModel> rsp = new TestRowHttpResponse(rowResultsAndNoErrors()) ) {
            parseResponse(rsp);
        }
    }

    private void parseResponse(Response<DefaultRowModel> rsp) {
        //noinspection StatementWithEmptyBody
        while (rsp.next() != null);
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
                "      \"message\": \"org.neo4j.kernel.api.exceptions.TransactionFailureException: Node record Node[381,used=false,rel=526,prop=-1,labels=Inline(0x0:[]),light] still has relationships\",\n" +
                "      \"stackTrace\": \"java.lang.RuntimeException: org.neo4j.kernel.api.exceptions.TransactionFailureException: Node record Node[381,used=false,rel=526,prop=-1,labels=Inline(0x0:[]),light] still has relationships\\n\\tat org.neo4j.server.rest.transactional.TransitionalTxManagementKernelTransaction.commit(TransitionalTxManagementKernelTransaction.java:87)\\n\\tat org.neo4j.server.rest.transactional.TransactionHandle.closeContextAndCollectErrors(TransactionHandle.java:279)\\n\\tat org.neo4j.server.rest.transactional.TransactionHandle.commit(TransactionHandle.java:148)\\n\\tat org.neo4j.server.rest.web.TransactionalService$2.write(TransactionalService.java:211)\\n\\tat com.sun.jersey.core.impl.provider.entity.StreamingOutputProvider.writeTo(StreamingOutputProvider.java:71)\\n\\tat com.sun.jersey.core.impl.provider.entity.StreamingOutputProvider.writeTo(StreamingOutputProvider.java:57)\\n\\tat com.sun.jersey.spi.container.ContainerResponse.write(ContainerResponse.java:302)\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl._handleRequest(WebApplicationImpl.java:1510)\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1419)\\n\\tat com.sun.jersey.server.impl.application.WebApplicationImpl.handleRequest(WebApplicationImpl.java:1409)\\n\\tat com.sun.jersey.spi.container.servlet.WebComponent.service(WebComponent.java:409)\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:558)\\n\\tat com.sun.jersey.spi.container.servlet.ServletContainer.service(ServletContainer.java:733)\\n\\tat javax.servlet.http.HttpServlet.service(HttpServlet.java:790)\\n\\tat org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:800)\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1669)\\n\\tat org.neo4j.server.rest.dbms.AuthorizationFilter.doFilter(AuthorizationFilter.java:116)\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1652)\\n\\tat org.neo4j.server.rest.web.CollectUserAgentFilter.doFilter(CollectUserAgentFilter.java:69)\\n\\tat org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1652)\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:585)\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:221)\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1125)\\n\\tat org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:515)\\n\\tat org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:185)\\n\\tat org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1059)\\n\\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141)\\n\\tat org.eclipse.jetty.server.handler.HandlerList.handle(HandlerList.java:52)\\n\\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:97)\\n\\tat org.eclipse.jetty.server.Server.handle(Server.java:497)\\n\\tat org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:310)\\n\\tat org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:248)\\n\\tat org.eclipse.jetty.io.AbstractConnection$2.run(AbstractConnection.java:540)\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:620)\\n\\tat org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:540)\\n\\tat java.lang.Thread.run(Thread.java:724)\\nCaused by: org.neo4j.kernel.api.exceptions.TransactionFailureException: Node record Node[381,used=false,rel=526,prop=-1,labels=Inline(0x0:[]),light] still has relationships\\n\\tat org.neo4j.kernel.impl.transaction.state.IntegrityValidator.validateNodeRecord(IntegrityValidator.java:52)\\n\\tat org.neo4j.kernel.impl.transaction.state.TransactionRecordState.extractCommands(TransactionRecordState.java:170)\\n\\tat org.neo4j.kernel.impl.api.KernelTransactionImplementation.commit(KernelTransactionImplementation.java:537)\\n\\tat org.neo4j.kernel.impl.api.KernelTransactionImplementation.close(KernelTransactionImplementation.java:456)\\n\\tat org.neo4j.server.rest.transactional.TransitionalTxManagementKernelTransaction.commit(TransitionalTxManagementKernelTransaction.java:83)\\n\\t... 35 more\\n\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream noResultsAndErrors() {
        String s = "{\n" +
                "  \"results\": [],\n" +
                "  \"errors\": [\n" +
                "    {\n" +
                "      \"code\": \"Neo.ClientError.Statement.InvalidSyntax\",\n" +
                "      \"message\": \"Invalid input 'T': expected 'a/A' (line 1, column 4 (offset: 3))\\n\\\"CRETE (_0:`School`:`DomainObject`{_0_props}) RETURN id(_0) AS _0 \\\"\\n    ^\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream rowResultsAndNoErrors() {

        final String s= "{\"results\": [{\"columns\": [\"collect(p)\"],\"data\": [{\"row\": [[[{\"name\": \"My Test\"}]]]}]}],\"errors\": []}";

        return new ByteArrayInputStream(s.getBytes());
    }

    private InputStream noRowResultsAndNoErrors() {

        final String s = "{\"results\": [{\"columns\": [\"collect(p)\"],\"data\": [{\"row\": [[]]}]}],\"errors\": []}";

        return new ByteArrayInputStream(s.getBytes());
    }


    static class TestRowHttpResponse extends AbstractHttpResponse<ResultRowModel> implements Response<DefaultRowModel> {

        public TestRowHttpResponse(InputStream inputStream) {
            super(inputStream, ResultRowModel.class);
        }

        @Override
        public DefaultRowModel next() {
            ResultRowModel rowModel = nextDataRecord("row");

            if (rowModel != null) {
                return new DefaultRowModel(rowModel.queryResults(), columns());
            }
            return null;
        }

        @Override
        public void close() {
            //Nothing to do, the response has been closed already
        }

    }
}
