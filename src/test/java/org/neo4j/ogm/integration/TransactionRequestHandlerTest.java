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

import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.ClassRule;
import org.junit.Test;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.session.result.ResultProcessingException;
import org.neo4j.ogm.session.transaction.LongTransaction;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Michal Bachman
 */
public class TransactionRequestHandlerTest
{

    @ClassRule
    public static Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule("2"); // idle tx killed after 2 secs

    private Session session;

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Test
    public void testCreateLongTransaction() {

        TransactionManager txRequestHandler = new TransactionManager(httpClient, neo4jRule.url());
        try (Transaction tx = txRequestHandler.openTransaction(null)) {
            assertEquals(Transaction.Status.OPEN, tx.status());
        }
    }

    @Test
    public void testCreateConcurrentTransactions() {

        TransactionManager txRequestHandler = new TransactionManager(httpClient, neo4jRule.url());

        // note that the try-with-resources implies these transactions are nested, but they are in fact independent
        try (Transaction tx1 = txRequestHandler.openTransaction(null)) {
            try (Transaction tx2 = txRequestHandler.openTransaction(null)) {
                assertEquals(Transaction.Status.OPEN, tx1.status());
                assertEquals(Transaction.Status.OPEN, tx2.status());
            }
        }
    }


    @Test(expected = ResultProcessingException.class)
    public void shouldDetectErrorsOnCommitOfLongRunningTransaction() {
        SessionFactory sessionFactory = new SessionFactory("");
        session = sessionFactory.openSession(neo4jRule.url());
        Transaction tx = session.beginTransaction();
        session.query("GARBAGE", Utils.map());
        tx.commit();
    }

    @Test(expected = ResultProcessingException.class)
    public void shouldDetectErrorsOnCommitOfNonExistentTransaction() {
        TransactionManager txRequestHandler = new TransactionManager(httpClient, neo4jRule.url());
        Transaction tx = new LongTransaction(null, neo4jRule.url(), txRequestHandler);
        tx.commit();
    }

    @Test
    public void shouldBeAbleToStartMultipleConcurrentLongRunningTransactions() throws InterruptedException {

        SessionFactory sessionFactory = new SessionFactory();
        session = sessionFactory.openSession(neo4jRule.url());

        int numThreads = 100;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch( numThreads );

        for (int i = 0; i < numThreads; i++) {
            executor.submit(new TransactionStarter(latch));
        }
        latch.await(); // pause until the count reaches 0
        System.out.println("all threads running");
        executor.shutdownNow();
    }

    @Test
    public void shouldRollbackExplicitTransactionWhenServerTransactionTimeout() throws InterruptedException {

        SessionFactory sessionFactory = new SessionFactory();
        session = sessionFactory.openSession(neo4jRule.url());

        try (Transaction tx = session.beginTransaction()) {
            // Wait for transaction to timeout on server
            Thread.sleep(3000);
            // Try to purge database using timed-out transaction
            session.purgeDatabase();
            fail("Should have caught exception");
        } catch (ResultProcessingException rpe) {
            HttpResponseException cause = (HttpResponseException) rpe.getCause();
            assertEquals("Not Found", cause.getMessage());
            assertEquals(404, cause.getStatusCode());
        }
        // should pass, because previous transaction will be closed by try block
        session.purgeDatabase();
    }

    class TransactionStarter implements Runnable {

        private final CountDownLatch latch;

        public TransactionStarter(CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run() {

            final Transaction tx = session.beginTransaction();
            System.out.println("opened a transaction: " + tx);
            latch.countDown();

            // run forever
            // but let the executor interrupt us to shut us down
            while(!Thread.currentThread().isInterrupted()){
                //do stuff
                try{
                    Thread.sleep(100);
                }catch(InterruptedException e){
                    System.out.println("Stopping thread");
                    tx.rollback();
                    Thread.currentThread().interrupt(); //propagate interrupt
                }
            }
        }
    }


}
