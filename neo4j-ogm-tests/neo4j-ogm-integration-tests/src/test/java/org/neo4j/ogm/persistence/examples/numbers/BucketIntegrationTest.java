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
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Frantisek Hartman
 */
public class BucketIntegrationTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.persistence.examples.numbers");
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
