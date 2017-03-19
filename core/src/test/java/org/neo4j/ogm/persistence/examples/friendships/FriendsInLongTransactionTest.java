/*
 * Copyright (c) 2002-2016 "Neo Technology,"
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

package org.neo4j.ogm.persistence.examples.friendships;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.friendships.Person;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Luanne Misquitta
 */
public class FriendsInLongTransactionTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.friendships");
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
            assertEquals(Transaction.Status.OPEN, tx.status());
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
            assertEquals(2, john.getFriends().size());
        }
    }
}
