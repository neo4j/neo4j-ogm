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

package org.neo4j.ogm.drivers.embedded.response;

import org.neo4j.graphdb.Result;
import org.neo4j.ogm.drivers.embedded.transaction.EmbeddedTransaction;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public abstract class EmbeddedResponse<T> implements Response {

    protected final Result result;
    private final Logger logger = LoggerFactory.getLogger(EmbeddedResponse.class);
    private final TransactionManager transactionManager;

    public EmbeddedResponse(Result result, TransactionManager transactionManager) {
        logger.debug("Response opened: {}", this);
        this.transactionManager = transactionManager;
        this.result = result;
    }

    @Override
    public abstract T next();

    @Override
    public void close() {

        // if there is no current transaction available, the response is already closed.
        // it is not an error to call close() multiple times, and in certain circumstances
        // it may be unavoidable.
        if (transactionManager.getCurrentTransaction() != null) {
            // release the response resource
            result.close();
            logger.debug("Response closed: {}", this);
            // if the current transaction is an autocommit one, we should commit and close it now,
            EmbeddedTransaction tx = (EmbeddedTransaction) transactionManager.getCurrentTransaction();
            if (tx.isAutoCommit()) {
                tx.commit();
                tx.close();
            }
        }
    }

    @Override
    public String[] columns() {
        return result.columns().toArray(new String[result.columns().size()]);
    }
}


