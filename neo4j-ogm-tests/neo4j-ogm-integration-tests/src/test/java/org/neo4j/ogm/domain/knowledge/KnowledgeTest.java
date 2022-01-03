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
package org.neo4j.ogm.domain.knowledge;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author vince
 */
public class KnowledgeTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.knowledge");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
    }

    /**
     * @see 351
     */
    @Test
    public void shouldBeAbleToRollbackObjectWithDifferentKnowledges() {

        Person john = new Person("John");
        Person mary = new Person("Mary");

        Language java = new Language("Java");
        Language scala = new Language("Scala");

        john.knows(mary);
        john.knows(java);
        john.knows(scala);

        try (Transaction tx = session.beginTransaction()) {

            session.save(john);

            tx.rollback();
        }
    }
}
