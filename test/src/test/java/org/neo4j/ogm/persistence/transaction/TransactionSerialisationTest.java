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

package org.neo4j.ogm.persistence.transaction;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.Utils;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author vince
 * @see Issue #130
 */
public class TransactionSerialisationTest extends MultiDriverTestClass {

    @Test
    public void shouldBeAbleToRunMultiThreadedLongRunningQueriesWithoutLosingConnectionResources() throws InterruptedException {

        int numThreads = Runtime.getRuntime().availableProcessors() * 4;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        String query = "FOREACH (n in RANGE(1, 1000) | CREATE (a)-[:KNOWS]->(b))";

        for (int i = 0; i < numThreads; i++) {
            executor.submit(new QueryRunner(latch, query));
        }
        latch.await(); // pause until the count reaches 0

        executor.shutdownNow();
    }

    @Test
    public void shouldBeAbleToHandleMultiThreadedFailingQueriesWithoutLosingConnectionResources() throws InterruptedException {

        int numThreads = Runtime.getRuntime().availableProcessors() * 4;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        // invalid query. should fail, response should be closed automatically, and connection released
        // if this does not happen, the session.query method in the query runner will block forever,
        // once the available connections are used up.
        String query = "FOREACH (n in RANGE(1, 1000) ? CREATE (a)-[:KNOWS]->(b))";

        for (int i = 0; i < numThreads; i++) {
            executor.submit(new QueryRunner(latch, query));
        }
        latch.await(); // pause until the count reaches 0
        executor.shutdownNow();
    }


    class QueryRunner implements Runnable {

        private final CountDownLatch latch;
        private final String query;
        private final SessionFactory sessionFactory = new SessionFactory(baseConfiguration, "org.neo4j.ogm.domain.tree");

        public QueryRunner(CountDownLatch latch, String query) {
            this.query = query;
            this.latch = latch;
        }

        @Override
        public void run() {

            Session session = sessionFactory.openSession();

            try (Transaction tx = session.beginTransaction()) {
                session.query(query, Utils.map());
                tx.commit();
            } catch (Exception e) {
                Assert.assertTrue(e instanceof CypherException);
            } finally {
                latch.countDown();
            }

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); //propagate interrupt
                }
            }
        }
    }
}
