/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.persistence.transaction;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.neo4j.ogm.exception.CypherException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.transaction.Transaction;

/**
 * GH-130
 * @author Vince Bickers
 */
public class TransactionSerialisationTest extends TestContainersTestBase {

    @Test
    public void shouldBeAbleToRunMultiThreadedLongRunningQueriesWithoutLosingConnectionResources()
        throws InterruptedException {

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
    public void shouldBeAbleToHandleMultiThreadedFailingQueriesWithoutLosingConnectionResources()
        throws InterruptedException {

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
        private final SessionFactory sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.tree");

        QueryRunner(CountDownLatch latch, String query) {
            this.query = query;
            this.latch = latch;
        }

        @Override
        public void run() {

            Session session = sessionFactory.openSession();

            try (Transaction tx = session.beginTransaction()) {
                session.query(query, Collections.emptyMap());
                tx.commit();
            } catch (Exception e) {
                assertThat(e instanceof CypherException).isTrue();
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
