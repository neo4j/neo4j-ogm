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

package org.neo4j.ogm.drivers.bolt.response;

import java.util.Set;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Luanne Misquitta
 */
public abstract class BoltResponse<T> implements Response {

    final StatementResult result;
    private final TransactionManager transactionManager;

    private final Logger LOGGER = LoggerFactory.getLogger(BoltResponse.class);

    BoltResponse(StatementResult result, TransactionManager transactionManager) {
        this.result = result;
        this.transactionManager = transactionManager;
    }

    @Override
    public T next() {
        try {
            return fetchNext();
        } catch (ClientException ce) {
            LOGGER.debug("Error executing Cypher: {}, {}", ce.code(), ce.getMessage());
            throw new CypherException("Error executing Cypher", ce, ce.code(), ce.getMessage());
        }
    }

    protected abstract T fetchNext();

    @Override
    public void close() {
        // if there is no current transaction available, the response is already closed.
        if (transactionManager.getCurrentTransaction() != null) {
            // release the response resource
            result.consume();
        }
    }

    @Override
    public String[] columns() {
        if (result.hasNext()) {
            Record record = result.peek();
            if (record != null) {
                Set<String> columns = result.peek().asMap().keySet();
                return columns.toArray(new String[columns.size()]);
            }
        }
        return new String[0];
    }
}
