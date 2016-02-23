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

package org.neo4j.ogm.session.transaction;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.neo4j.ogm.authentication.CredentialsService;
import org.neo4j.ogm.authentication.HttpRequestAuthorization;
import org.neo4j.ogm.authentication.Neo4jCredentials;
import org.neo4j.ogm.authentication.UsernamePasswordCredentials;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.result.ErrorsException;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class TransactionManager {

    private final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
    private final CloseableHttpClient httpClient;
    private final String url;
    private final Neo4jCredentials credentials;

    private static final ThreadLocal<Transaction> transaction = new ThreadLocal<>();

    public TransactionManager(CloseableHttpClient httpClient, String server) {
        this.url = transactionRequestEndpoint(server);
        this.httpClient = httpClient;
        this.credentials = CredentialsService.userNameAndPassword();
        transaction.remove(); // ensures this thread does not have a current tx;
    }

    public TransactionManager(CloseableHttpClient httpClient, String server, UsernamePasswordCredentials credentials) {
        this.url = transactionRequestEndpoint(server);
        this.httpClient = httpClient;
        this.credentials = credentials;
        transaction.remove(); // ensures this thread does not have a current tx;
    }

    public Transaction openTransaction(MappingContext mappingContext) {
        String transactionEndpoint = newTransactionEndpointUrl();
        logger.debug("Creating new transaction with endpoint {}", transactionEndpoint);
        transaction.set(new LongTransaction(mappingContext, transactionEndpoint, this));
        return transaction.get();
    }

    public void rollback(Transaction tx) {
        String url = tx.url();
        logger.debug("DELETE {}", url);
        HttpDelete request = new HttpDelete(url);
        try (CloseableHttpResponse response = executeRequest(request)) {;
        } catch ( ResultProcessingException rpe ) {
            logger.warn( "Rollback request failed: " + rpe.getCause().getLocalizedMessage());
            throw rpe;
        } catch ( IOException ioe )
        {
            throw new RuntimeException( "Could not close response after rollback request: ", ioe  );
        }
    }

    public void commit(Transaction tx) {
        String url = tx.url() + "/commit";
        logger.debug("POST {}", url);
        HttpPost request = new HttpPost(url);
        request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
        try (CloseableHttpResponse response = executeRequest(request)) {;
            response.close();
        } catch ( ResultProcessingException rpe ) {
            logger.warn( "Commit request failed: " + rpe.getCause().getLocalizedMessage());
            throw rpe;
        } catch ( IOException ioe )
        {
            throw new RuntimeException( "Could not close response after commit request: ", ioe  );
        }
    }

    public Transaction getCurrentTransaction() {
        return transaction.get();
    }

    private CloseableHttpResponse executeRequest(HttpRequestBase request) {

        CloseableHttpResponse response = null;

        try {

            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));
            HttpRequestAuthorization.authorize(request, credentials);

            response = httpClient.execute(request);
            StatusLine statusLine = response.getStatusLine();

            logger.debug("Status code: {}", statusLine.getStatusCode());

            if (statusLine.getStatusCode() >= 300) {
                throw new HttpResponseException(
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }

            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                String responseText = EntityUtils.toString(responseEntity);
                logger.debug(responseText);
                EntityUtils.consume(responseEntity);
                if (responseText.contains("\"errors\":[{") || responseText.contains("\"errors\": [{")) {
                    throw new ErrorsException(responseText);
                }
            }
            return response;
        }

        catch (Exception e) {
            if (response != null) {
                try
                {
                    response.close();
                } catch (IOException ioe) {
                    throw new RuntimeException( "Failed to close response after exception: ", ioe );
                }
            }
            throw new ResultProcessingException("Failed to execute request: ", e);
        }

        // always clean up the connection and the thread-local transaction instance;
        finally {
            request.releaseConnection();
            transaction.remove();
        }
    }

    private String newTransactionEndpointUrl() {
        logger.debug("POST {}", url);
        HttpPost request = new HttpPost(url);
        request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
        CloseableHttpResponse response = executeRequest(request);
        Header location = response.getHeaders("Location")[0];
        try {
            response.close();
        } catch (Exception ioe) {
            throw new RuntimeException( "Could not close response: ", ioe);
        }
        return location.getValue();
    }

    private String transactionRequestEndpoint(String server) {
        if (server == null) {
            return server;
        }
        String url = server;

        if (!server.endsWith("/")) {
            url += "/";
        }
        return url + "db/data/transaction";
    }

    public void status() {

    }
}
