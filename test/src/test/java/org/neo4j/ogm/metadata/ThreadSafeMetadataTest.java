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

package org.neo4j.ogm.metadata;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.neo4j.ogm.domain.pizza.Pizza;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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
