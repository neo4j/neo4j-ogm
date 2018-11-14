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

package org.neo4j.ogm.drivers.http.driver;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.config.ObjectMapperFactory;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.http.request.HttpRequest;
import org.neo4j.ogm.drivers.http.request.HttpRequestException;
import org.neo4j.ogm.drivers.http.transaction.HttpTransaction;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.request.DefaultRequest;
import org.neo4j.ogm.request.OptimisticLockingConfig;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.request.Statement;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author vince
 */

public final class HttpDriver extends AbstractConfigurableDriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDriver.class);
    private final ObjectMapper mapper = ObjectMapperFactory.objectMapper();

    private CloseableHttpClient httpClient;

    public HttpDriver() {
    }

    public HttpDriver(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void configure(Configuration configuration) {
        super.configure(configuration);

        if (configuration.getVerifyConnection()) {
            httpClient();

            HttpRequest request = new HttpRequest(httpClient(), requestUrl(), this.configuration.getCredentials(), true);
            request.execute(new VerifyRequest());
        }
    }

    @Override
    protected String getTypeSystemName() {
        throw new UnsupportedOperationException("The HTTP Driver doesn't support a native type system.");
    }

    @Override
    public synchronized void close() {
        try {
            LOGGER.info("Shutting down Http driver {} ", this);
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
            LOGGER.warn("Unexpected Exception when closing http client httpClient: ", e);
        }
    }

    @Override
    public Request request() {
        Transaction tx = transactionManager.getCurrentTransaction();
        if (tx == null) {
            return new HttpRequest(httpClient(), requestUrl(), configuration.getCredentials());
        } else {
            return new HttpRequest(httpClient(), requestUrl(), configuration.getCredentials(), tx.isReadOnly());
        }
    }

    @Override
    public Transaction newTransaction(Transaction.Type type, Iterable<String> bookmarks) {
        if (bookmarks != null && bookmarks.iterator().hasNext()) {
            LOGGER.warn("Passing bookmarks {} to HttpDriver. This is not currently supported.", bookmarks);
        }
        return new HttpTransaction(transactionManager, this, newTransactionUrl(type), type);
    }

    public CloseableHttpResponse executeHttpRequest(HttpRequestBase request) throws HttpRequestException {

        try (CloseableHttpResponse response = HttpRequest
            .execute(httpClient(), request, configuration.getCredentials())) {
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                JsonNode responseNode = mapper.readTree(EntityUtils.toString(responseEntity));
                LOGGER.debug("Response: {}", responseNode);
                JsonNode errors = responseNode.findValue("errors");
                if (errors.elements().hasNext()) {
                    JsonNode errorNode = errors.elements().next();
                    throw new CypherException("Error executing Cypher",
                        errorNode.findValue("code").asText(), errorNode.findValue("message").asText());
                }
            }
            return response;
        } catch (IOException ioe) {
            throw new HttpRequestException(request, ioe);
        } finally {
            request.releaseConnection();
            LOGGER.debug("Thread: {}, Connection released", Thread.currentThread().getId());
        }
    }

    private String newTransactionUrl(Transaction.Type type) {

        String url = transactionEndpoint(configuration.getURI());
        LOGGER.debug("Thread: {}, POST {}", Thread.currentThread().getId(), url);

        HttpPost request = new HttpPost(url);
        request.setHeader("X-WRITE", type == Transaction.Type.READ_ONLY ? "0" : "1");

        try (CloseableHttpResponse response = executeHttpRequest(request)) {
            Header location = response.getHeaders("Location")[0];
            return location.getValue();
        } catch (IOException ioe) {
            throw new HttpRequestException(request, ioe);
        }
    }

    private String autoCommitUrl() {
        return transactionEndpoint(configuration.getURI()).concat("/commit");
    }

    private String transactionEndpoint(String server) {
        if (server == null) {
            return null;
        }
        String url = server;

        if (!server.endsWith("/")) {
            url += "/";
        }
        return url + "db/data/transaction";
    }

    private String requestUrl() {
        if (transactionManager != null) {
            Transaction tx = transactionManager.getCurrentTransaction();
            if (tx != null) {
                LOGGER
                    .debug("Thread: {}, request url {}", Thread.currentThread().getId(), ((HttpTransaction) tx).url());
                return ((HttpTransaction) tx).url();
            } else {
                LOGGER.debug("Thread: {}, No current transaction, using auto-commit", Thread.currentThread().getId());
            }
        } else {
            LOGGER.debug("Thread: {}, No transaction manager available, using auto-commit",
                Thread.currentThread().getId());
        }
        LOGGER.debug("Thread: {}, request url {}", Thread.currentThread().getId(), autoCommitUrl());
        return autoCommitUrl();
    }

    public boolean readOnly() {
        if (transactionManager != null) {
            Transaction tx = transactionManager.getCurrentTransaction();
            if (tx != null) {
                return tx.isReadOnly();
            }
        }
        return false; // its read-write by default
    }

    @Override
    public boolean requiresTransaction() {
        return false;
    }

    private synchronized CloseableHttpClient httpClient() {

        if (httpClient
            == null) {   // most of the time this will be false, branch-prediction will be very fast and the lock released immediately

            try {
                HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

                SSLContext sslContext = SSLContext.getDefault();

                if (configuration.getTrustStrategy() != null) {

                    if (configuration.getTrustStrategy().equals("ACCEPT_UNSIGNED")) {
                        sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();

                        LOGGER.warn("Certificate validation has been disabled");
                    }
                }

                // setup the default or custom ssl context
                httpClientBuilder.setSSLContext(sslContext);

                HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();

                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
                    hostnameVerifier);
                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();

                // allows multi-threaded use
                PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                    socketFactoryRegistry);

                Integer connectionPoolSize = configuration.getConnectionPoolSize();

                connectionManager.setMaxTotal(connectionPoolSize);
                connectionManager.setDefaultMaxPerRoute(connectionPoolSize);

                httpClientBuilder.setConnectionManager(connectionManager);

                httpClient = httpClientBuilder.build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return httpClient;
    }

    private static class VerifyRequest implements DefaultRequest {
        @Override
        public List<Statement> getStatements() {
            return Collections.singletonList(new Statement() {
                @Override
                public String getStatement() {
                    return "RETURN 1";
                }

                @Override
                public Map<String, Object> getParameters() {
                    return Collections.emptyMap();
                }

                @Override
                public String[] getResultDataContents() {
                    return new String[0];
                }

                @Override
                public boolean isIncludeStats() {
                    return false;
                }

                @Override
                public Optional<OptimisticLockingConfig> optimisticLockingConfig() {
                    return Optional.empty();
                }
            });
        }
    }
}
