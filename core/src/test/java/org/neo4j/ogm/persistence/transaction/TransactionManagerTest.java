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

package org.neo4j.ogm.persistence.transaction;

import static org.junit.Assert.*;

import org.junit.Test;
import org.neo4j.ogm.exception.TransactionManagerException;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.transaction.DefaultTransactionManager;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.transaction.Transaction;

/**
 * Transactions in the OGM
 * A managed transaction is one whose lifecycle is managed via a TransactionManager instance
 * Transactions managed by a Transaction Manager will automatically:
 * 1)  maintain single per-thread transaction instances
 * 2)  manage the calls to the appropriate driver to implement the relevant transaction semantics
 * 3)  manage transaction lifecycle and state correctly
 *
 * @author Michal Bachman
 * @author Vince Bickers
 */
public class TransactionManagerTest extends MultiDriverTestClass {

	private DefaultTransactionManager transactionManager = new DefaultTransactionManager(null);

	@Test
	public void shouldBeAbleToCreateManagedTransaction() {
		try (Transaction tx = transactionManager.openTransaction()) {
			assertEquals(Transaction.Status.OPEN, tx.status());
		}
	}

	@Test(expected = TransactionManagerException.class)
	public void shouldFailCommitFreeTransactionInManagedContext() {
		try (Transaction tx = Components.driver().newTransaction(Transaction.Type.READ_WRITE, null)) {
			transactionManager.commit(tx);
		}
	}

	@Test(expected = TransactionManagerException.class)
	public void shouldFailRollbackFreeTransactionInManagedContext() {
		try (Transaction tx = Components.driver().newTransaction(Transaction.Type.READ_WRITE, null)) {
			transactionManager.rollback(tx);
		}
	}

	@Test
	public void shouldRollbackManagedTransaction() {
		try (Transaction tx = transactionManager.openTransaction()) {
			assertEquals(Transaction.Status.OPEN, tx.status());
			tx.rollback();
			assertEquals(Transaction.Status.ROLLEDBACK, tx.status());
		}
	}

}
