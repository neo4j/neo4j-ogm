/*
 * Copyright (c) 2002-2017 "Neo Technology,"
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.ogm.drivers.bolt.transaction.BoltTransaction;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.response.Response;
import org.neo4j.ogm.transaction.TransactionManager;

/**
 * @author Luanne Misquitta
 */
public abstract class BoltResponse<T> implements Response {

	protected final StatementResult result;
	private final TransactionManager transactionManager;


	private final Logger LOGGER = LoggerFactory.getLogger(BoltResponse.class);


	public BoltResponse(StatementResult result, TransactionManager transactionManager) {
		this.result = result;
		this.transactionManager = transactionManager;
	}

	@Override
	public T next() {
		try {
			return fetchNext();
		} catch (ClientException ce) {
			BoltTransaction tx = (BoltTransaction) transactionManager.getCurrentTransaction();
			if (tx != null) {
				tx.rollback();
			}
			LOGGER.debug("Error executing Cypher: {}, {}", ce.neo4jErrorCode(), ce.getMessage());
			throw new CypherException("Error executing Cypher", ce, ce.neo4jErrorCode(), ce.getMessage());
		}
	}

	public abstract T fetchNext();

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
        try {
        	if (result.hasNext()) {
				Record record = result.peek();
				if (record != null) {
					Set<String> columns = result.peek().asMap().keySet();
					return columns.toArray(new String[columns.size()]);
				}
            }
        } catch (ClientException ce) {
            // exception may occur if records has not been fetched yet
            // should we catch other things than ClientException ?
            BoltTransaction tx = (BoltTransaction) transactionManager.getCurrentTransaction();
            if (tx != null) {
                tx.rollback();
            }
            LOGGER.debug("Error executing Cypher: {}, {}", ce.code(), ce.getMessage());
            throw new CypherException("Error executing Cypher", ce, ce.code(), ce.getMessage());
        }

        return new String[0];
    }
}
