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

package org.neo4j.ogm.drivers.http.transaction;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author vince
 */
public class HttpTransaction extends AbstractTransaction {

    private final HttpDriver driver;
    private final String url;

    public HttpTransaction(TransactionManager transactionManager, HttpDriver driver, String url) {
        super(transactionManager);
        this.driver = driver;
        this.url = url;
    }

    @Override
    public void rollback() {

        try {
            if (transactionManager.canRollback()) {
                HttpDelete request = new HttpDelete(url);
                driver.executeHttpRequest(request);
            }
        }
        catch (Exception e) {
            throw new TransactionException(e.getLocalizedMessage());
        }
        finally {
            super.rollback(); // must always be done to keep extension depth correct
        }
    }

    @Override
    public void commit() {

        try {
            if (transactionManager.canCommit()) {
                HttpPost request = new HttpPost(url + "/commit");
                request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE,"application/json;charset=UTF-8"));
                driver.executeHttpRequest(request);
            } else {
                if (transactionManager.isExtended(transactionManager.getCurrentTransaction())) {
                    throw new TransactionException("Transaction is marked for rollback");
                }
            }
        }
        catch (Exception e) {
            throw new TransactionException(e.getLocalizedMessage());
        }
        finally {
            super.commit(); // must always be done to keep extension depth correct
        }
    }

    public String url() {
        return url;
    }
}
