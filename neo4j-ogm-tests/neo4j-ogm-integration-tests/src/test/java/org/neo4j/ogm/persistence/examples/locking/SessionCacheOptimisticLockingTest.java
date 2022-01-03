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
package org.neo4j.ogm.persistence.examples.locking;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.locking.FriendOf;
import org.neo4j.ogm.domain.locking.User;
import org.neo4j.ogm.domain.locking.VersionedEntityWithExternalId;
import org.neo4j.ogm.exception.OptimisticLockingException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * Test of behaviour of the Session cache when optimistic locking failures happen
 *
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class SessionCacheOptimisticLockingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void setUpClass() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.locking");
    }

    @Before
    public void setUp() {
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

    @Test
    public void lockingWithExternalIdsShouldWorkInDifferentSessions() throws InterruptedException {

        final String cypherTemplate = "MATCH (n:VersionedEntityWithExternalId) WHERE n.name = 'Michael' RETURN n";

        VersionedEntityWithExternalId emp = new VersionedEntityWithExternalId();
        emp.setName("Michael");
        session.save(emp);

        CountDownLatch t1latch = new CountDownLatch(1);
        CountDownLatch t2latch = new CountDownLatch(1);
        CountDownLatch outer = new CountDownLatch(2);

        AtomicBoolean gotException = new AtomicBoolean(false);
        new Thread(() -> {
            try {
                Session session1 = sessionFactory.openSession();

                VersionedEntityWithExternalId t1emp = session1.queryForObject(VersionedEntityWithExternalId.class,
                    cypherTemplate, Collections.emptyMap());
                t1emp.setSomething(
                    "OGM needs some real change here to trigger updates. " + ThreadLocalRandom.current().nextLong());

                t2latch.countDown();
                try {
                    t1latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // This is necessary. It opens a new session that is not aware of the thing with external id as in
                // it doesn't know that it has already been assigned an internal id as well.
                session1 = sessionFactory.openSession();
                session1.save(t1emp);
            } catch (OptimisticLockingException e) {
                gotException.set(true);
            } finally {
                outer.countDown();
            }

        }).start();

        new Thread(() -> {
            try {
                try {
                    t2latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Session session2 = sessionFactory.openSession();
                VersionedEntityWithExternalId t2emp = session2.queryForObject(VersionedEntityWithExternalId.class,
                    cypherTemplate, Collections.emptyMap());
                t2emp.setSomething(
                    "OGM needs some real change here to trigger updates. " + ThreadLocalRandom.current().nextLong());

                session2.save(t2emp);
                t1latch.countDown();
            } catch (OptimisticLockingException e) {
                gotException.set(true);
            } finally {
                outer.countDown();
            }
        }).start();

        outer.await();

        assertThat(gotException.get()).isTrue();
    }
}
