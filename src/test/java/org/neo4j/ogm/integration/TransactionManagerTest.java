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

package org.neo4j.ogm.integration;

import org.junit.Test;
import org.neo4j.ogm.api.driver.Driver;
import org.neo4j.ogm.api.transaction.Transaction;
import org.neo4j.ogm.session.transaction.DefaultTransactionManager;
import org.neo4j.ogm.session.transaction.TransactionManagerException;
import org.neo4j.ogm.spi.DriverService;

import static org.junit.Assert.assertEquals;

/**
 * Transactions in the OGM can be free or managed.
 *
 * A managed transaction is one whose lifecycle is managed via a TransactionManager instance
 *
 * Transactions managed by a Transaction Manager will automatically:
 *
 *  1)  maintain single per-thread transaction instances
 *  2)  ...
 *  3)  ...
 *
 * A free transaction is one obtained directly from a relevant driver, that the user must manage themselves.
 * The use of free transactions is not recommended in user code.
 *
 * @author Michal Bachman
 * @author Vince Bickers
 */
public class TransactionManagerTest {

    private static final Driver driver = DriverService.load("org.neo4j.ogm.driver.embedded.driver.EmbeddedDriver");
    private static final DefaultTransactionManager transactionManager = new DefaultTransactionManager(driver);

    @Test
    public void shouldBeAbleToCreateManagedTransaction() {
        try (Transaction tx = transactionManager.openTransaction()) {
            assertEquals(Transaction.Status.OPEN, tx.status());
        }
    }

    @Test(expected = TransactionManagerException.class)
    public void shouldNotBeAbleToCreateConcurrentOrNestedManagedTransactions() {
        try (Transaction tx1 = transactionManager.openTransaction()) {
            try (Transaction tx2 = transactionManager.openTransaction()) {
                assertEquals(Transaction.Status.OPEN, tx1.status());
                assertEquals(Transaction.Status.OPEN, tx2.status());
            }
        }
    }

    @Test(expected = TransactionManagerException.class)
    public void shouldFailCommitFreeTransactionInManagedContext() {
        Transaction tx = driver.newTransaction();
        transactionManager.commit(tx);
    }

    @Test(expected = TransactionManagerException.class)
    public void shouldFailRollbackFreeTransactionInManagedContext() {
        Transaction tx = driver.newTransaction();
        transactionManager.rollback(tx);
    }

    @Test
    public void shouldCommitFreeTransaction() {
        driver.setTransactionManager(null);
        Transaction tx = driver.newTransaction();
        tx.commit();
    }

    @Test
    public void shouldRollbackFreeTransaction() {
        driver.setTransactionManager(null);
        Transaction tx = driver.newTransaction();
        tx.rollback();
    }


}
