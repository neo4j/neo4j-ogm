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

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Collection;
import java.util.Date;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.locking.EnemyOf;
import org.neo4j.ogm.domain.locking.FriendOf;
import org.neo4j.ogm.domain.locking.User;
import org.neo4j.ogm.exception.OptimisticLockingException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Frantisek Hartman
 */
public class RelationshipEntityOptimisticLockingTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;

    private Session session;

    @BeforeClass
    public static void setUpClass() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.locking");
    }

    @Before
    public void setUp() throws Exception {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void whenSaveRelationshipEntityThenSetVersionToZero() {
        User michael = new User("Michael");
        User oliver = new User("Oliver");
        FriendOf friendOf = michael.addFriend(oliver);

        session.save(michael);

        assertThat(friendOf.getVersion()).isEqualTo(0L);
    }

    @Test
    public void givenRelationshipEntityWhenUpdateThenIncrementVersion() {
        User michael = new User("Michael");
        User oliver = new User("Oliver");
        FriendOf friendOf = michael.addFriend(oliver);

        session.save(michael);

        Date sinceUpdated = new Date();
        friendOf.setSince(sinceUpdated);

        session.save(michael);
        assertThat(friendOf.getVersion()).isEqualTo(1L);

        session.clear();

        FriendOf loaded = session.load(FriendOf.class, friendOf.getId());
        assertThat(loaded.getSince()).isEqualTo(sinceUpdated);
        assertThat(loaded.getVersion()).isEqualTo(1L);
    }

    @Test
    public void givenRelationshipEntityWithWrongVersionWhenSaveThenFailWithOptimisticLockingException() {
        User michael = new User("Michael");
        User oliver = new User("Oliver");
        FriendOf friendOf = michael.addFriend(oliver);

        session.save(michael);
        friendOf.setSince(new Date());
        session.save(friendOf);

        FriendOf wrongVersion = new FriendOf(michael, oliver);
        wrongVersion.setId(friendOf.getId());
        wrongVersion.setVersion(0L);
        Date updatedSince = new Date();
        wrongVersion.setSince(updatedSince);

        assertThatThrownBy(() -> session.save(wrongVersion, 0))
            .isInstanceOf(OptimisticLockingException.class);
    }

    @Test
    public void givenRelationshipEntityCustomThenOptimisticLockingCheckWorksAndThrowsException() {
        User michael = new User("Michael");
        User oliver = new User("Oliver");

        EnemyOf enemyOf = new EnemyOf(michael, oliver, new Date());
        session.save(enemyOf);
        assertThat(enemyOf.getCustomVersion()).isEqualTo(0L);

        enemyOf.setSince(new Date());
        session.save(enemyOf);
        assertThat(enemyOf.getCustomVersion()).isEqualTo(1L);

        EnemyOf wrongVersion = new EnemyOf(michael, oliver, new Date());
        wrongVersion.setId(enemyOf.getId());
        wrongVersion.setCustomVersion(0L);
        assertThatThrownBy(() -> session.save(wrongVersion, 0))
            .isInstanceOf(OptimisticLockingException.class);
    }

    @Test
    public void givenRelationshipEntityWhenDeleteThenDeleteRelationshipEntity() {
        User michael = new User("Michael");
        User oliver = new User("Oliver");
        FriendOf friendOf = michael.addFriend(oliver);

        session.save(michael);

        session.delete(friendOf);

        Collection<FriendOf> friendOfs = session.loadAll(FriendOf.class);
        assertThat(friendOfs).isEmpty();
    }

    @Test
    public void saveDeletedRelationshipEntityShouldFailWithOptimisticLockingException() {
        User michael = new User("Michael");
        User oliver = new User("Oliver");
        FriendOf friendOf = michael.addFriend(oliver);

        session.save(michael);

        // someone else deletes node
        session.delete(friendOf);

        friendOf.setSince(new Date());

        // save should throw exception
        assertThatThrownBy(() -> session.save(friendOf))
            .isInstanceOf(OptimisticLockingException.class);

        // save through related entity should trow exception
        assertThatThrownBy(() -> session.save(michael))
            .isInstanceOf(OptimisticLockingException.class);

        // and relationship should not exist
        Collection<FriendOf> friendOfs = session.loadAll(FriendOf.class);
        assertThat(friendOfs).isEmpty();
    }

    @Test
    public void givenRelationshipEntityWithWrongVersionWhenDeleteThenThrowOptimisticLockingException() {
        User michael = new User("Michael");
        User oliver = new User("Oliver");
        FriendOf friendOf = michael.addFriend(oliver);

        session.save(friendOf);

        friendOf.setVersion(1L);

        assertThatThrownBy(() -> session.delete(friendOf))
            .isInstanceOf(OptimisticLockingException.class);

        session.clear();
        Collection<FriendOf> friendOfs = session.loadAll(FriendOf.class);
        assertThat(friendOfs).hasSize(1);
    }

    @Test
    public void optimisticLockingExceptionShouldRollbackDefaultTransaction() {
        User michael = new User("Michael");
        User oliver = new User("Oliver");
        FriendOf friendOf = michael.addFriend(oliver);

        session.save(michael);
        friendOf.setSince(new Date());
        session.save(friendOf);

        michael.setName("Michael Updated");
        oliver.setName("Oliver Updated");
        FriendOf wrongVersion = new FriendOf(michael, oliver);
        wrongVersion.setId(friendOf.getId());
        wrongVersion.setVersion(0L);
        Date updatedSince = new Date();
        wrongVersion.setSince(updatedSince);

        try {
            session.save(wrongVersion, 0);
            fail("Expected OptimisticLockingException");
        } catch (OptimisticLockingException ex) {
            session.clear();

            Collection<User> users = session.loadAll(User.class);
            assertThat(users).extracting(User::getName).containsOnly("Michael", "Oliver");
        }
    }

    @Test
    public void removeVersionedRelationship() {
        User michael = new User("Michael");
        User oliver = new User("Oliver");
        FriendOf friendOf = michael.addFriend(oliver);

        session.save(michael);

        michael.clearFriends();

        session.save(michael);

        ensureFriendsOfRelationshipsHaveCount(0);
    }

    @Test // GH-746
    public void updateVersionedRelationship() {
        User michael = new User("Michael");
        User oliver = new User("Oliver");
        FriendOf friendOf = michael.addFriend(oliver);

        session.save(michael);

        oliver.clearFriends();

        User george = new User("George");
        george.addFriend(friendOf);
        session.save(michael);

        ensureFriendsOfRelationshipsHaveCount(1);
    }

    private void ensureFriendsOfRelationshipsHaveCount(long count) {
        session.clear();
        Long relationshipCount = (Long) session.query("MATCH ()-[r:FRIEND_OF]->() return count(r) as c", emptyMap())
            .queryResults().iterator().next().get("c");
        assertThat(relationshipCount).isEqualTo(count);
    }

}
