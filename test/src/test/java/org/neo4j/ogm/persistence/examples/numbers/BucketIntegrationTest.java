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

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Frantisek Hartman
 */
public class BucketIntegrationTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.persistence.examples.numbers");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @After
    public void tearDown() throws Exception {
        session.purgeDatabase();
    }

    @Test
    public void savedBucketShouldHaveDefaultValue() throws Exception {
        Bucket bucket = new Bucket();
        session.save(bucket);
        session.clear();

        Bucket loaded = session.load(Bucket.class, bucket.getId());
        assertThat(loaded.getNumbers()).containsOnly(1, 2, 3);
    }

    @Test
    public void emptiedBucketShouldBeEmptyAfterReload() throws Exception {
        Bucket bucket = new Bucket();
        bucket.setNumbers(emptyList());
        session.save(bucket);
        session.clear();

        Bucket loaded = session.load(Bucket.class, bucket.getId());
        assertThat(loaded.getNumbers()).isEmpty();
    }
}
