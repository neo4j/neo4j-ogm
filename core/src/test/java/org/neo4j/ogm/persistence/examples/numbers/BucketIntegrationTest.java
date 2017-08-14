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

package org.neo4j.ogm.persistence.examples.numbers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import static java.util.Collections.emptyList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Frantisek Hartman
 */
public class BucketIntegrationTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory("org.neo4j.ogm.persistence.examples.numbers");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void savedBucketShouldHaveDefaultValue() throws Exception {
        Bucket bucket = new Bucket();
        session.save(bucket);
        session.clear();

        Bucket loaded = session.load(Bucket.class, bucket.getId());
        assertEquals(Arrays.asList(1, 2, 3), loaded.getNumbers());
    }

    @Test
    public void emptiedBucketShouldBeEmptyAfterReload() throws Exception {
        Bucket bucket = new Bucket();
        bucket.setNumbers(new ArrayList<Integer>());
        session.save(bucket);
        session.clear();

        Bucket loaded = session.load(Bucket.class, bucket.getId());
        assertTrue(loaded.getNumbers().isEmpty());
    }
}
