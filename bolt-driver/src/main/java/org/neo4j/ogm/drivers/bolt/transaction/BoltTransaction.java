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

package org.neo4j.ogm.drivers.bolt.transaction;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.ogm.exception.ConnectionException;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.transaction.AbstractTransaction;
import org.neo4j.ogm.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vince
 */
public class BoltTransaction extends AbstractTransaction {

	public static final String NEO_CLIENT_ERROR_SECURITY = "Neo.ClientError.Security";
	private final Transaction nativeTransaction;
	private final Session nativeSession;
	private final Logger LOGGER = LoggerFactory.getLogger(BoltTransaction.class);

	public BoltTransaction(TransactionManager transactionManager, Transaction transaction, Session session) {
		super(transactionManager);
		this.nativeTransaction = transaction;
		this.nativeSession = session;
	}

	@Override
	public void rollback() {
		try {
			if (transactionManager.canRollback()) {
				LOGGER.debug("Rolling back native transaction: {}", nativeTransaction);
				nativeTransaction.failure();
				nativeTransaction.close();
				nativeSession.close();
			}
		} catch (ClientException ce) {
			throw new CypherException("Error executing Cypher", ce, ce.neo4jErrorCode(), ce.getMessage());
		} catch (Exception e) {
			throw new TransactionException(e.getLocalizedMessage());
		} finally {
			super.rollback();
		}
	}

	@Override
	public void commit() {
		try {
			if (transactionManager.canCommit()) {
				LOGGER.debug("Committing native transaction: {}", nativeTransaction);
				nativeTransaction.success();
				nativeTransaction.close();
				nativeSession.close();
			}
		} catch (ClientException ce) {
			nativeSession.close();
			if (ce.neo4jErrorCode().startsWith(NEO_CLIENT_ERROR_SECURITY)) {
				throw new ConnectionException("Security Error: " + ce.neo4jErrorCode() + ", " + ce.getMessage(), ce);
			}
			throw new CypherException("Error executing Cypher", ce, ce.neo4jErrorCode(), ce.getMessage());
		} catch (Exception e) {
			nativeSession.close();
			throw new TransactionException(e.getLocalizedMessage());
		} finally {
			super.commit();
		}
	}

	public Transaction nativeBoltTransaction() {
		return nativeTransaction;
	}
}
