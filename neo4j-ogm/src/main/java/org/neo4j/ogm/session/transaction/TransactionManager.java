/*
 * Copyright (c) 2002-2015 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j-OGM.
 *
 * Neo4j-OGM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.ogm.session.transaction;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionManager {

    private final Logger logger = LoggerFactory.getLogger(TransactionManager.class);
    private final CloseableHttpClient httpClient;
    private final String url;

    private static final ThreadLocal<Transaction> transaction = new ThreadLocal<>();

    public TransactionManager(CloseableHttpClient httpClient, String server) {
        this.url = transactionRequestEndpoint(server);
        this.httpClient = httpClient;
        transaction.remove(); // ensures this thread does not have a current tx;
    }

    public Transaction openTransaction(MappingContext mappingContext) {
        String transactionEndpoint = newTransactionEndpointUrl();
        logger.info("Creating new transaction with endpoint " + transactionEndpoint);
        transaction.set(new LongTransaction(mappingContext, transactionEndpoint, this));
        return transaction.get();
    }

    public void rollback(Transaction tx) {
        String url = tx.url();
        logger.info("DELETE " + url);
        HttpDelete request = new HttpDelete(url);
        executeRequest(request);
        transaction.remove();
    }

    public void commit(Transaction tx) {
        String url = tx.url() + "/commit";
        logger.info("POST " + url);
        HttpPost request = new HttpPost(url);
        request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
        executeRequest(request);
        transaction.remove();
    }

    public Transaction getCurrentTransaction() {
        return transaction.get();
    }

    private HttpResponse executeRequest(HttpRequestBase request) {
        try {

            request.setHeader(new BasicHeader("Accept", "application/json;charset=UTF-8"));

            HttpResponse response = httpClient.execute(request);
            StatusLine statusLine = response.getStatusLine();

            logger.info("Status code: " + statusLine.getStatusCode());
            if (statusLine.getStatusCode() >= 300) {
                throw new HttpResponseException(
                        statusLine.getStatusCode(),
                        statusLine.getReasonPhrase());
            }
            // we're not interested in the content, but we must always close the content stream/release the connection
            try {
                HttpEntity responseEntity = response.getEntity();

                if (responseEntity != null) {
                    String responseText = EntityUtils.toString(responseEntity);
                    logger.info(responseText);
                    EntityUtils.consume(responseEntity);
                }
                else {
                    request.releaseConnection();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return response;
        }
        catch (Exception e) {
            throw new ResultProcessingException("Failed to execute request: ", e);
        }
    }

    private String newTransactionEndpointUrl() {
        logger.info("POST " + url);
        HttpPost request = new HttpPost(url);
        request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
        HttpResponse response = executeRequest(request);
        Header location = response.getHeaders("Location")[0];
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
}
