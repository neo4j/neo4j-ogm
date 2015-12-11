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

package org.neo4j.ogm.persistence;

import org.junit.Test;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.exception.ResultProcessingException;
import org.neo4j.ogm.exception.TransactionManagerException;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.transaction.DefaultTransactionManager;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.transaction.Transaction;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Transactions in the OGM
 * <p/>
 * A managed transaction is one whose lifecycle is managed via a TransactionManager instance
 * <p/>
 * Transactions managed by a Transaction Manager will automatically:
 * <p/>
 * 1)  maintain single per-thread transaction instances
 * 2)  manage the calls to the appropriate driver to implement the relevant transaction semantics
 * 3)  manage transaction lifecycle and state correctly
 *
 * @author Michal Bachman
 * @author Vince Bickers
 */
public class TransactionManagerTest extends MultiDriverTestClass {

    private static final DefaultTransactionManager transactionManager = new DefaultTransactionManager();

    @Test
    public void shouldBeAbleToCreateManagedTransaction() {
        try (Transaction tx = transactionManager.openTransaction()) {
            assertEquals(Transaction.Status.OPEN, tx.status());
        }
    }

    public void shouldBeAbleToExtendTransactions() {
        try (Transaction tx1 = transactionManager.openTransaction()) {
            try (Transaction tx2 = transactionManager.openTransaction()) {
                assertEquals(Transaction.Status.OPEN, tx1.status());
                assertEquals(Transaction.Status.OPEN, tx2.status());
            }
        }
    }

    @Test(expected = TransactionManagerException.class)
    public void shouldFailCommitFreeTransactionInManagedContext() {
        Transaction tx = Components.driver().newTransaction();
        transactionManager.commit(tx);
    }

    @Test(expected = TransactionManagerException.class)
    public void shouldFailRollbackFreeTransactionInManagedContext() {
        Transaction tx = Components.driver().newTransaction();
        transactionManager.rollback(tx);
    }

    @Test
    public void shouldRollbackManagedTransaction() {
        try (Transaction tx = transactionManager.openTransaction()) {
            assertEquals(Transaction.Status.OPEN, tx.status());
            tx.rollback();
            assertEquals(Transaction.Status.ROLLEDBACK, tx.status());
        }
    }

    @Test
    public void shouldBeAbleToStartMultipleConcurrentLongRunningTransactions() throws InterruptedException {

        int numThreads = 100;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(new TransactionStarter(latch));
        }
        latch.await(); // pause until the count reaches 0
        System.out.println("all threads running");

        // force termination of all threads
        executor.shutdownNow();


    }

    @Test
    public void shouldRollbackExplicitTransactionWhenServerTransactionTimeout() throws InterruptedException {

        // we can't force the embedded instance tx timeout to < 60 seconds,
        // and we're not gonna wait that long.
        if (Components.driver() instanceof HttpDriver) {

            SessionFactory sessionFactory = new SessionFactory();
            Session session = sessionFactory.openSession();

            // the transaction manager must manage transactions
            try (Transaction tx = transactionManager.openTransaction()) {
                // Wait for transaction to timeout on server
                Thread.sleep(3000);
                // Try to purge database using timed-out transaction
                session.purgeDatabase();
                fail("Should have caught exception");
            } catch (ResultProcessingException rpe) {
                //HttpResponseException cause = (HttpResponseException) rpe.getCause();
                //assertEquals("Not Found", cause.getMessage());
                //assertEquals(404, cause.getStatusCode());
            }
            // should pass, because previous transaction will be closed by try block
            session.purgeDatabase();
        }
    }

    class TransactionStarter implements Runnable {

        private final CountDownLatch latch;

        public TransactionStarter(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {

            final Transaction tx = Components.driver().newTransaction();
            System.out.println("opened a transaction: " + tx);
            latch.countDown();

            // run forever
            // but let the executor interrupt us to shut us down
            while (!Thread.currentThread().isInterrupted()) {
                //do stuff
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("Stopping thread");
                    transactionManager.rollback(tx);
                    Thread.currentThread().interrupt(); //propagate interrupt
                }
            }
        }
    }
}
