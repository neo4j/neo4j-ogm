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

package org.neo4j.ogm.persistence;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.ogm.drivers.http.driver.HttpDriver;
import org.neo4j.ogm.drivers.http.request.HttpRequestException;
import org.neo4j.ogm.exception.TransactionException;
import org.neo4j.ogm.exception.TransactionManagerException;
import org.neo4j.ogm.service.Components;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
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

    private Session session;
    private DefaultTransactionManager transactionManager = new DefaultTransactionManager(session);


    @Test
    public void shouldBeAbleToCreateManagedTransaction() {
        try (Transaction tx = transactionManager.openTransaction()) {
            assertEquals(Transaction.Status.OPEN, tx.status());
        }
        Transaction tx = transactionManager.getCurrentTransaction();

    }

    @Test(expected = TransactionManagerException.class)
    public void shouldFailCommitFreeTransactionInManagedContext() {
        try (Transaction tx = Components.driver().newTransaction()) {
            transactionManager.commit(tx);
        }
    }

    @Test(expected = TransactionManagerException.class)
    public void shouldFailRollbackFreeTransactionInManagedContext() {
        try (Transaction tx = Components.driver().newTransaction()) {
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

    @Test
    public void shouldBeAbleToStartMultipleConcurrentLongRunningTransactions() throws InterruptedException {

        int numThreads = 100;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(new TransactionStarter(latch));
        }
        latch.await(); // pause until the count reaches 0

        // force termination of all threads
        executor.shutdownNow();


    }

    @Test
    public void shouldRollbackExplicitTransactionWhenServerTransactionTimeout() throws InterruptedException {

        // we can't force the embedded instance tx timeout to < 60 seconds,
        // and we're not gonna wait that long.
        if (Components.driver() instanceof HttpDriver) {

            SessionFactory sessionFactory = new SessionFactory();
            session = sessionFactory.openSession();

            // the transaction manager must manage transactions
            try (Transaction tx = transactionManager.openTransaction()) {
                // Wait for transaction to timeout on server
                Thread.sleep(3000);
                // Try to purge database using timed-out transaction
                session.purgeDatabase();
                fail("Should have caught exception");
            } catch (HttpRequestException rpe) {
                // expected
            }
            // should pass, because previous transaction will be closed by try block
            session.purgeDatabase();
        }
    }

    @Test
    public void shouldBeAbleToRunMultiThreadedLongRunningQueriesWithoutLosingConnectionResources() throws InterruptedException {

        SessionFactory sessionFactory = new SessionFactory();
        session = sessionFactory.openSession();

        int numThreads = Runtime.getRuntime().availableProcessors() * 4;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch( numThreads );

        // valid query. should succeed, response should be closed automatically, and connection released
        // if this does not happen, the session.query method in the query runner will block forever, once
        // the available connections are used up.
        String query = "FOREACH (n in RANGE(1, 10000) | CREATE (a)-[:KNOWS]->(b))";
        long now = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            executor.submit(new QueryRunner(latch, query));
        }
        latch.await(); // pause until the count reaches 0
        executor.shutdownNow();
        System.out.println( "elapsed: " + ( System.currentTimeMillis() - now));
    }

    @Test
    public void shouldBeAbleToHandleMultiThreadedFailingQueriesWithoutLosingConnectionResources() throws InterruptedException {

        SessionFactory sessionFactory = new SessionFactory();
        session = sessionFactory.openSession();

        int numThreads = Runtime.getRuntime().availableProcessors() * 4;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch( numThreads );

        // invalid query. should fail, response should be closed automatically, and connection released
        // if this does not happen, the session.query method in the query runner will block forever,
        // once the available connections are used up.
        String query = "FOREACH (n in RANGE(1, 10000) ? CREATE (a)-[:KNOWS]->(b))";

        for (int i = 0; i < numThreads; i++) {
            executor.submit(new QueryRunner(latch, query));
        }
        latch.await(); // pause until the count reaches 0
        executor.shutdownNow();
    }

    @Test
    public void shouldNotBeReadOnlyByDefault()  {

        SessionFactory sessionFactory = new SessionFactory();
        session = sessionFactory.openSession();

        Transaction tx = session.beginTransaction();
        Assert.assertFalse(tx.isReadOnly());
    }

    @Test
    public void shouldBeAbleToCreateReadOnlyTransaction()  {

        SessionFactory sessionFactory = new SessionFactory();
        session = sessionFactory.openSession();

        Transaction tx = session.beginTransaction(Transaction.Type.READ_ONLY);
        Assert.assertTrue(tx.isReadOnly());
    }

    @Test
    public void shouldNotBeAbleToExtendAReadTransactionWithAReadWriteInnerTransaction()  {

        SessionFactory sessionFactory = new SessionFactory();
        session = sessionFactory.openSession();

        try {
            Transaction tx1 = session.beginTransaction(Transaction.Type.READ_ONLY);
            Transaction tx2 = session.beginTransaction(Transaction.Type.READ_WRITE);
            fail("Should not have allowed transaction extension of different type");
        } catch (TransactionException tme) {
            Assert.assertEquals("Incompatible transaction type specified: must be 'READ_ONLY'", tme.getLocalizedMessage());
        }
    }

    @Test
    public void shouldNotBeAbleToExtendAReadWriteTransactionWithAReadOnlyInnerTransaction()  {

        SessionFactory sessionFactory = new SessionFactory();
        session = sessionFactory.openSession();

        try {
            Transaction tx1 = session.beginTransaction(Transaction.Type.READ_WRITE);
            Transaction tx2 = session.beginTransaction(Transaction.Type.READ_ONLY);
            fail("Should not have allowed transaction extension of different type");
        } catch (TransactionException tme) {
            Assert.assertEquals("Incompatible transaction type specified: must be 'READ_WRITE'", tme.getLocalizedMessage());
        }
    }

    @Test
    public void shouldAutomaticallyExtendAReadOnlyTransactionWithAReadOnlyExtension()  {

        SessionFactory sessionFactory = new SessionFactory();
        session = sessionFactory.openSession();

        Transaction tx1 = session.beginTransaction(Transaction.Type.READ_ONLY);
        Transaction tx2 = session.beginTransaction();
        Assert.assertTrue(tx2.isReadOnly());
    }

    @Test
    public void shouldAutomaticallyExtendAReadWriteTransactionWithAReadWriteExtension()  {

        SessionFactory sessionFactory = new SessionFactory();
        session = sessionFactory.openSession();

        Transaction tx1 = session.beginTransaction(Transaction.Type.READ_WRITE);
        Transaction tx2 = session.beginTransaction();
        Assert.assertFalse(tx2.isReadOnly());
    }


    class QueryRunner implements Runnable {

        private final CountDownLatch latch;
        private final String query;

        public QueryRunner( CountDownLatch latch, String query ) {
            this.query = query;
            this.latch = latch;
        }

        @Override
        public void run() {

            try
            {
                session.query( query, Utils.map() );
                System.out.println( Thread.currentThread().getName() + ": ran successfully" );
            } catch (Exception e)
            {
                System.out.println( Thread.currentThread().getName() + ": caught exception ");
            }
            finally {
                System.out.println( Thread.currentThread().getName() + ": finished" );
                latch.countDown();
            }

            while(!Thread.currentThread().isInterrupted()){
                try{
                    Thread.sleep(100);
                } catch(InterruptedException e){
                    Thread.currentThread().interrupt(); //propagate interrupt
                }
            }

        }
    }

    class TransactionStarter implements Runnable {

        private final CountDownLatch latch;

        public TransactionStarter(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {
            try(Transaction tx = Components.driver().newTransaction()) {
                latch.countDown();
                // run forever
                // but let the executor interrupt us to shut us down
                while (!Thread.currentThread().isInterrupted()) {
                    //do stuff
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        tx.rollback();
                        transactionManager.rollback(tx);
                        Thread.currentThread().interrupt(); //propagate interrupt
                    }
                }
            }
        }
    }
}
