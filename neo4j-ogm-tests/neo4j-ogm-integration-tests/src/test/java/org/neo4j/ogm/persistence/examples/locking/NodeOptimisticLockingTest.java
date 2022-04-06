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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.locking.FriendOf;
import org.neo4j.ogm.domain.locking.Location;
import org.neo4j.ogm.domain.locking.PowerUser;
import org.neo4j.ogm.domain.locking.User;
import org.neo4j.ogm.exception.OptimisticLockingException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Frantisek Hartman
 * @author Michael J. Simons
 */
public class NodeOptimisticLockingTest extends TestContainersTestBase {

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
    public void whenSaveNewNodeThenSetVersionToZero() {
        User frantisek = new User("Frantisek");

        // version set in the entity
        session.save(frantisek);
        assertThat(frantisek.getVersion()).isEqualTo(0L);

        // version set in the graph
        session.clear();
        User loaded = session.load(User.class, frantisek.getId());
        assertThat(loaded.getVersion()).isEqualTo(0L);
    }

    @Test
    public void givenNodeWhenUpdateNodeThenIncrementVersion() {
        User frantisek = new User("Frantisek");

        session.save(frantisek);
        frantisek.setName("Frantisek Hartman");
        session.save(frantisek);

        // version updated
        assertThat(frantisek.getVersion()).isEqualTo(1L);

        session.clear();
        User loaded = session.load(User.class, frantisek.getId());
        assertThat(loaded.getName()).isEqualTo("Frantisek Hartman");
        assertThat(loaded.getVersion()).isEqualTo(1L);
    }

    @Test
    public void givenNodeWithWrongVersionWhenSaveNodeThenFailWithOptimisticLockingException() {
        User frantisek = new User("Frantisek");
        session.save(frantisek);
        frantisek.setName("Frantisek Hartman");
        session.save(frantisek);

        User wrongVersion = new User("Frantisek");
        wrongVersion.setId(frantisek.getId());
        wrongVersion.setVersion(0L);

        assertThatThrownBy(() -> session.save(wrongVersion))
            .isInstanceOf(OptimisticLockingException.class)
            .hasMessageContaining("Entity with type='[User]'")
            .hasMessageContaining("id='" + frantisek.getId() + "' had incorrect version 0");
    }

    @Test
    public void saveOnNonExistingEntityShouldFailWithOptimisticLockingException() {
        User frantisek = new User("Frantisek");
        session.save(frantisek);

        // someone else deletes node
        session.delete(frantisek);

        frantisek.setName("Frantisek Hartman");

        // save should throw exception
        assertThatThrownBy(() -> session.save(frantisek))
            .isInstanceOf(OptimisticLockingException.class);

        // and node should not exist
        Collection<User> users = session.loadAll(User.class);
        assertThat(users).isEmpty();
    }

    @Test
    public void givenNodeWhenDeleteThenNodeIsDeleted() {
        // This is normal delete, but should still work with optimistic locking
        User frantisek = new User("Frantisek");
        session.save(frantisek);

        session.delete(frantisek);

        Collection<User> users = session.loadAll(User.class);
        assertThat(users).isEmpty();
    }

    @Test
    public void givenNodeWithWrongVersionWhenDeleteThenThrowOptimisticLockingException() {
        User frantisek = new User("Frantisek");
        session.save(frantisek);

        frantisek.setVersion(1L);

        assertThatThrownBy(() -> session.delete(frantisek))
            .isInstanceOf(OptimisticLockingException.class);
    }

    @Test
    public void shouldWorkWithInheritedVersionField() {
        PowerUser frantisek = new PowerUser("Frantisek");
        session.save(frantisek);

        assertThat(frantisek.getVersion()).isEqualTo(0L);

        frantisek.setName("Frantisek Hartman");
        session.save(frantisek);

        assertThat(frantisek.getVersion()).isEqualTo(1L);
    }

    @Test
    public void shouldWorkWithCustomVersionFieldName() {
        Location london = new Location("London");
        session.save(london);

        assertThat(london.getCustomVersion()).isEqualTo(0L);

        london.setName("Greater London");
        session.save(london);

        assertThat(london.getCustomVersion()).isEqualTo(1L);
    }

    @Test // GH-894
    public void assertWorkingLocksOnReReadWithChangedProperties() {
        // Use independent sessions
        Session session1 = sessionFactory.openSession();
        long existingId = session1.queryForObject(Long.class, "CREATE (a:Location {name: 'Aachen', customVersion: 1}) RETURN id(a)", Collections.emptyMap());
        session1.clear();

        Session session2 = sessionFactory.openSession();

        // 1. Read the node with id 123 from db ,version =1
        Location location = session2.load(Location.class, existingId);
        assertThat(location.getCustomVersion()).isOne();

        // 2. in the same session later part of the code flow .read the same node with id 123 from db
        // updated properties and invoked session save, version has incremented to 2 (version=2)
        // Re-Read on purpose, see ticket
        location = session2.load(Location.class, existingId);
        location.setName("Aachen Brand");
        session2.save(location);
        assertThat(location.getCustomVersion()).isEqualTo(2L);

        long version;
        version = session1.queryForObject(Long.class, "MATCH (a:Location {name: $name}) RETURN a.customVersion", Collections.singletonMap("name", location.getName()));
        session1.clear();
        assertThat(version).isEqualTo(2L);

        // 3. the object which got at step 1. added relation and updated properties
        // Re-Read on purpose, see ticket
        location = session2.load(Location.class, existingId);
        location.setName("Aachen-Brand");
        session2.save(location);
        assertThat(location.getCustomVersion()).isEqualTo(3L);

        version = session1.queryForObject(Long.class, "MATCH (a:Location {name: $name}) RETURN a.customVersion", Collections.singletonMap("name", location.getName()));
        session1.clear();
        assertThat(version).isEqualTo(3L);
    }

    @Test // GH-894
    public void assertWorkingLocksOnReReadWithChangedRelationships() {
        // Use independent sessions
        Session session1 = sessionFactory.openSession();
        long bertId = session1.queryForObject(Long.class, "CREATE (a:User {name: 'bert', version: 1}) RETURN id(a)", Collections.emptyMap());
        long ernieId = session1.queryForObject(Long.class, "CREATE (a:User {name: 'Ernie', version: 1}) RETURN id(a)", Collections.emptyMap());
        session1.clear();

        Session session2 = sessionFactory.openSession();

        // 1. Read the node with id 123 from db ,version =1
        User bert = session2.load(User.class, bertId);
        assertThat(bert.getVersion()).isOne();

        User ernie = session2.load(User.class, ernieId);
        assertThat(ernie.getVersion()).isOne();

        // 2. in the same session later part of the code flow .read the same node with id 123 from db
        // updated properties and invoked session save, version has incremented to 2 (version=2)
        // Re-Read on purpose, see ticket
        bert = session2.load(User.class, bertId);
        bert.setName("Bert");
        FriendOf friendOf = bert.addFriend(ernie);
        session2.save(bert);
        assertThat(bert.getVersion()).isEqualTo(2L);

        Map<String, Object> versions;
        versions = session1.query("MATCH (a:User {name: $name}) -[f:FRIEND_OF] -> (:User) RETURN"
            + " a.version as userVersion, f.version as relVersion", Collections.singletonMap("name", bert.getName()))
            .queryResults().iterator().next();
        session1.clear();
        assertThat(versions).containsEntry("userVersion", 2L);
        assertThat(versions).containsEntry("relVersion", 0L);

        // 3. the object which got at step 1. added relation and updated properties
        // Re-Read on purpose, see ticket
        bert = session2.load(User.class, bertId);
        bert.setName("Bertram");
        session2.save(bert);
        assertThat(bert.getVersion()).isEqualTo(3L);

        versions = session1.query("MATCH (a:User {name: $name}) -[f:FRIEND_OF] -> (:User) RETURN"
                + " a.version as userVersion, f.version as relVersion", Collections.singletonMap("name", bert.getName()))
            .queryResults().iterator().next();
        session1.clear();
        assertThat(versions).containsEntry("userVersion", 3L);
        assertThat(versions).containsEntry("relVersion", 0L);

        friendOf.setDescription("an updated description");

        session2.save(bert);
        assertThat(bert.getVersion()).isEqualTo(3L);

        versions = session1.query("MATCH (a:User {name: $name}) -[f:FRIEND_OF] -> (:User) RETURN"
                + " a.version as userVersion, f.version as relVersion, f.description as description", Collections.singletonMap("name", bert.getName()))
            .queryResults().iterator().next();
        session1.clear();
        assertThat(versions).containsEntry("userVersion", 3L);
        assertThat(versions).containsEntry("relVersion", 1L);
        assertThat(versions).containsEntry("description", "an updated description");
    }
}
