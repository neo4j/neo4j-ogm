/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.session;

import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.cypher.ComparisonOperator;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.domain.concurrency.World;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Mark Angrish
 */
public class ConcurrentSessionTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    boolean failed = false;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.concurrency");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
    }

    @Test
    public void multipleThreadsResultsGetMixedUp() throws Exception {

        World world1 = new World("world 1", 1);
        session.save(world1, 0);

        World world2 = new World("world 2", 2);
        session.save(world2, 0);

        int iterations = 1000;

        ExecutorService service = Executors.newFixedThreadPool(2);
        final CountDownLatch countDownLatch = new CountDownLatch(iterations * 2);

        for (int i = 0; i < iterations; i++) {

            service.execute(() -> {
                World world = session.loadAll(World.class, new Filter("name", ComparisonOperator.EQUALS, "world 1"))
                    .iterator().next();

                if (!"world 1".equals(world.getName())) {
                    failed = true;
                }
                countDownLatch.countDown();
            });

            service.execute(() -> {

                World world = session.loadAll(World.class, new Filter("name", ComparisonOperator.EQUALS, "world 2"))
                    .iterator().next();

                if (!"world 2".equals(world.getName())) {
                    failed = true;
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        assertThat(failed).isFalse();
    }
}
