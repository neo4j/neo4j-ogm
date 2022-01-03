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
package org.neo4j.ogm.metadata;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.neo4j.ogm.domain.pizza.Pizza;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Frantisek Hartman
 */
public class ThreadSafeMetadataTest {

    private static final Logger logger = LoggerFactory.getLogger(ThreadSafeMetadataTest.class);

    private static final int THREADS = 8;
    private static final int TOTAL_RUNS = 100;

    @Test
    public void testGetFieldInfoThreadSafe() throws Exception {

        ExecutorService service = Executors.newFixedThreadPool(THREADS);

        for (int i = 0; i < TOTAL_RUNS; i++) {

            CountDownLatch latch = new CountDownLatch(THREADS);
            List<Callable<Object>> runnables = new ArrayList<>();
            MetaData metaData = new MetaData("org.neo4j.ogm.domain.pizza");
            for (int j = 0; j < THREADS; j++) {
                runnables.add(() -> {
                    FieldInfo fieldInfo = metaData.classInfo(Pizza.class.getName()).getFieldInfo("name");
                    if (fieldInfo != null) {
                        latch.countDown();
                    }
                    return null;
                });
            }

            service.invokeAll(runnables);
            latch.await(1, TimeUnit.SECONDS);
            assertThat(latch.getCount()).isZero();

            logger.info("Metadata init with multiple threads successful");
        }

        service.shutdown();
    }
}
