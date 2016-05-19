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

package org.neo4j.ogm.drivers.http.driver;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpResponseException;
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
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.neo4j.ogm.driver.AbstractConfigurableDriver;
import org.neo4j.ogm.drivers.http.request.HttpRequest;
import org.neo4j.ogm.drivers.http.transaction.HttpTransaction;
import org.neo4j.ogm.exception.ResultErrorsException;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.request.Request;
import org.neo4j.ogm.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.security.cert.X509Certificate;

/**
 * @author vince
 */

public final class HttpDriver extends AbstractConfigurableDriver
{
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDriver.class);

    private CloseableHttpClient httpClient;

    public HttpDriver() {}

    public HttpDriver( CloseableHttpClient httpClient ) {
        this.httpClient = httpClient;
    }

    @Override
    public synchronized void close() {
        try {
            LOGGER.info("Shutting down Http driver {} ", this);
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (Exception e) {
            LOGGER.warn( "Unexpected Exception when closing http client httpClient: ", e );
        }
    }

    @Override
    public Request request() {
        String url = requestUrl();
        return new HttpRequest(httpClient(), url, driverConfig.getCredentials());
    }

    @Override
    public Transaction newTransaction() {

        String url = newTransactionUrl();
        return new HttpTransaction(transactionManager, this, url);
    }

    public CloseableHttpResponse executeHttpRequest(HttpRequestBase request) {

        try {
            try(CloseableHttpResponse response = HttpRequest.execute(httpClient(), request, driverConfig.getCredentials())) {
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String responseText = EntityUtils.toString(responseEntity);
                    LOGGER.debug("Thread {}: {}", Thread.currentThread().getId(), responseText );
                    EntityUtils.consume(responseEntity);
                    if (responseText.contains("\"errors\":[{") || responseText.contains("\"errors\": [{")) {
                        throw new ResultErrorsException(responseText);
                    }
                }
                return response;
            }
        }

        catch (HttpResponseException hre) {
            if (hre.getStatusCode() == 404) {
                Transaction tx = transactionManager.getCurrentTransaction();
                if (tx != null) {
                    transactionManager.rollback(tx);
                }
            }
            throw new ResultProcessingException("HttpResponse exception: Not Found", hre);
        }

        catch (Exception e) {
            throw new ResultProcessingException("Failed to execute request: ", e);
        }

        finally {
            request.releaseConnection();
            LOGGER.debug( "Thread {}: Connection released", Thread.currentThread().getId() );
        }
    }

    private String newTransactionUrl() {

        String url = transactionEndpoint(driverConfig.getURI());
        LOGGER.debug( "Thread {}: POST {}", Thread.currentThread().getId(), url );

        try (CloseableHttpResponse response = executeHttpRequest(new HttpPost(url))) {
            Header location = response.getHeaders("Location")[0];
            response.close();
            return location.getValue();
        } catch (Exception e) {
            throw new ResultProcessingException("Could not obtain new Transaction: ", e);
        }
    }

    private String autoCommitUrl() {
        return transactionEndpoint(driverConfig.getURI()).concat("/commit");
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
                LOGGER.debug("Thread {}: request url {}", Thread.currentThread().getId(), ((HttpTransaction) tx).url());
                return ((HttpTransaction) tx).url();
            } else {
                LOGGER.debug( "Thread {}: No current transaction, using auto-commit", Thread.currentThread().getId() );
            }
        } else {
            LOGGER.debug( "Thread {}: No transaction manager available, using auto-commit", Thread.currentThread().getId() );
        }
        LOGGER.debug( "Thread {}: request url {}", Thread.currentThread().getId(), autoCommitUrl() );
        return autoCommitUrl();
    }

    private synchronized CloseableHttpClient httpClient()  {

        if (httpClient == null) {   // most of the time this will be false, branch-prediction will be very fast and the lock released immediately

            try {
                HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

                SSLContext sslContext = SSLContext.getDefault();

                if (driverConfig.getTrustStrategy() != null) {

                    if (driverConfig.getTrustStrategy().equals("ACCEPT_UNSIGNED")) {
                        sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                            public boolean isTrusted(X509Certificate[] arg0, String arg1) {
                                return true;
                            }
                        }).build();

                        LOGGER.warn("Certificate validation has been disabled");
                    }
                }

                // setup the default or custom ssl context
                httpClientBuilder.setSSLContext(sslContext);

                HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.getDefaultHostnameVerifier();

                SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.getSocketFactory())
                        .register("https", sslSocketFactory)
                        .build();

                // allows multi-threaded use
                PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);

                Integer connectionPoolSize = driverConfig.getConnectionPoolSize();

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
}
