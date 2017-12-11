/*
 * Copyright (c) 2002-2018 "Neo Technology,"
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

package org.neo4j.ogm.persistence.examples.locking;

import static org.assertj.core.api.Assertions.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.locking.FriendOf;
import org.neo4j.ogm.domain.locking.User;
import org.neo4j.ogm.exception.OptimisticLockingException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * Test of behaviour of the Session cache when optimistic locking failures happen
 *
 * @author Frantisek Hartman
 */
public class SessionCacheOptimisticLockingTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void setUpClass() {
        sessionFactory = new SessionFactory(driver, "org.neo4j.ogm.domain.locking");
    }

    @Before
    public void setUp() throws Exception {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldLoadNewNodeVersionInSessionAfterFailureToSave() {
        Session session1 = sessionFactory.openSession();
        User frantisek = new User("Frantisek");
        session1.save(frantisek);

        Session session2 = sessionFactory.openSession();
        User updated = new User("Frantisek The Ugly");
        updated.setId(frantisek.getId());
        updated.setVersion(0L);
        session2.save(updated);

        try {
            frantisek.setName("Frantisek The Mighty");
            session1.save(frantisek);
            fail("Should have thrown OptimisticLockingException");
        } catch (OptimisticLockingException e) {

            // failed update should remove the instance from Session, on reload we should get the updated instance

            User loaded = session1.load(User.class, frantisek.getId());
            assertThat(loaded.getName()).isEqualTo("Frantisek The Ugly");
        }
    }

    /**
     * Same case as {@link #shouldLoadNewNodeVersionInSessionAfterFailureToSave()}, only the operation is `delete`
     */
    @Test
    public void shouldLoadNewNodeVersionInSessionAfterFailureToDelete() {
        Session session1 = sessionFactory.openSession();
        User frantisek = new User("Frantisek");
        session1.save(frantisek);

        Session session2 = sessionFactory.openSession();
        User updated = new User("Frantisek The Ugly");
        updated.setId(frantisek.getId());
        updated.setVersion(0L);
        session2.save(updated);

        try {
            session1.delete(frantisek);
            fail("Should have thrown OptimisticLockingException");
        } catch (OptimisticLockingException e) {

            // failed update should remove the instance from Session, on reload we should get the updated instance

            User loaded = session1.load(User.class, frantisek.getId());
            assertThat(loaded.getName()).isEqualTo("Frantisek The Ugly");

            // after reload we should be able to delete
            session1.delete(loaded);

            loaded = session1.load(User.class, frantisek.getId());
            assertThat(loaded).isNull();
        }

    }

    @Test
    public void shouldLoadNewRelationshipVersionInSessionAfterFailureToSave() {
        Session session1 = sessionFactory.openSession();

        User michael = new User("Michael");
        User oliver = new User("Oliver");
        FriendOf friendOf = michael.addFriend(oliver);
        friendOf.setDescription("m-o");
        session1.save(friendOf);

        Session session2 = sessionFactory.openSession();
        FriendOf updated = new FriendOf(michael, oliver);
        updated.setDescription("updated session 2");
        updated.setId(friendOf.getId());
        updated.setVersion(0L);
        session2.save(updated, 0);

        try {

            friendOf.setDescription("updated session 1");
            session1.save(friendOf, 0);

            fail("Should have thrown OptimisticLockingException");
        } catch (OptimisticLockingException e) {

            // failed update should remove the instance from Session, on reload we should get the updated instance

            FriendOf loaded = session1.load(FriendOf.class, friendOf.getId());
            assertThat(loaded.getDescription()).isEqualTo("updated session 2");
        }
    }

    @Test
    public void shouldLoadNewRelationshipVersionInSessionAfterFailureToDelete() {
        Session session1 = sessionFactory.openSession();

        User michael = new User("Michael");
        User oliver = new User("Oliver");
        FriendOf friendOf = michael.addFriend(oliver);
        friendOf.setDescription("m-o");
        session1.save(friendOf);

        Session session2 = sessionFactory.openSession();
        FriendOf updated = new FriendOf(michael, oliver);
        updated.setDescription("updated session 2");
        updated.setId(friendOf.getId());
        updated.setVersion(0L);
        session2.save(updated, 0);

        try {

            session1.delete(friendOf);

            fail("Should have thrown OptimisticLockingException");
        } catch (OptimisticLockingException e) {

            // failed update should remove the instance from Session, on reload we should get the updated instance

            FriendOf loaded = session1.load(FriendOf.class, friendOf.getId());
            assertThat(loaded.getDescription()).isEqualTo("updated session 2");

            session1.delete(loaded);


            loaded = session1.load(FriendOf.class, friendOf.getId());
            assertThat(loaded).isNull();
        }
    }

    @Test
    public void shouldLoadNewRelationshipVersionInSessionAfterFailureToDeleteBySave() {
        Session session1 = sessionFactory.openSession();

        User alice = new User("Alice");
        User bob = new User("Bob");
        FriendOf friendOf = alice.addFriend(bob);
        friendOf.setDescription("a-b");
        session1.save(friendOf);

        Session session2 = sessionFactory.openSession();
        FriendOf updated = new FriendOf(alice, bob);
        updated.setDescription("updated session 2");
        updated.setId(friendOf.getId());
        updated.setVersion(0L);
        session2.save(updated, 0);

        try {

            // session1 has old version of friendOf in session, should fail
            alice.clearFriends();
            bob.clearFriends();
            session1.save(alice);

            fail("Should have thrown OptimisticLockingException");
        } catch (OptimisticLockingException e) {

            // failed update should remove the instance from Session, on reload we should get the updated instance

            FriendOf loaded = session1.load(FriendOf.class, friendOf.getId());
            assertThat(loaded.getDescription()).isEqualTo("updated session 2");

            session1.delete(loaded);


            loaded = session1.load(FriendOf.class, friendOf.getId());
            assertThat(loaded).isNull();
        }
    }


}
