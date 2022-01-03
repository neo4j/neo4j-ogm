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
package org.neo4j.ogm.persistence.examples.friendships;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.friendships.Person;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Luanne Misquitta
 */
public class FriendsInLongTransactionTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.friendships");
    }

    @Before
    public void init() {
        session = sessionFactory.openSession();
    }

    /**
     * @see DATAGRAPH-703
     */
    @Test
    public void createPersonAndFriendsInLongTransaction() {
        try (Transaction tx = session.beginTransaction()) {
            assertThat(tx.status()).isEqualTo(Transaction.Status.OPEN);
            Person john = new Person("John");
            session.save(john);

            Person bob = new Person("Bob");
            session.save(bob);

            Person bill = new Person("Bill");
            session.save(bill);

            john = session.load(Person.class, john.getId());
            bob = session.load(Person.class, bob.getId());
            john.addFriend(bob);
            session.save(john);

            john = session.load(Person.class, john.getId());
            bill = session.load(Person.class, bill.getId());
            john.addFriend(bill);
            session.save(john);

            session.clear();
            session.load(Person.class, john.getId());
            assertThat(john.getFriends()).hasSize(2);
        }
    }
}
