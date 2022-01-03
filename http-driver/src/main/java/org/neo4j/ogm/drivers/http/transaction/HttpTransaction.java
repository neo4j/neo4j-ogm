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
package org.neo4j.ogm.drivers.http.transaction;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.Transaction;
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Vince Bickers
 * @author Michael J. Simons
 */
public class HttpTransaction extends AbstractTransaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpDriver.class);

    private final HttpDriver driver;
    private final String url;

    public HttpTransaction(TransactionManager transactionManager, HttpDriver driver, String url,
        Transaction.Type type) {
        super(transactionManager);
        this.driver = driver;
        this.url = url;
        this.type = type;
    }

    @Override
    public void rollback() {

        try {
            if (transactionManager.canRollback()) {
                HttpDelete request = new HttpDelete(url);
                request.setHeader(new BasicHeader("X-WRITE", readOnly() ? "0" : "1"));
                driver.executeHttpRequest(request);
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage());
        } finally {
            super.rollback(); // must always be done to keep extension depth correct
        }
    }

    @Override
    public void commit() {

        try {
            if (transactionManager.canCommit()) {
                HttpPost request = new HttpPost(url + "/commit");
                request.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));
                request.setHeader(new BasicHeader("X-WRITE", readOnly() ? "0" : "1"));
                driver.executeHttpRequest(request);
            }
        } catch (Exception e) {
            throw new TransactionException(e.getLocalizedMessage(), e);
        } finally {
            super.commit(); // must always be done to keep extension depth correct
        }
    }

    private boolean readOnly() {
        if (transactionManager != null) {
            Transaction tx = transactionManager.getCurrentTransaction();
            if (tx != null) {
                return tx.isReadOnly();
            }
        }
        return false; // its read-write by default
    }

    public String url() {
        return url;
    }
}
