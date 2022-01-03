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

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.friendships.Friendship;
import org.neo4j.ogm.domain.friendships.Person;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class FriendshipsRelationshipEntityTest extends TestContainersTestBase {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getDriver(), "org.neo4j.ogm.domain.friendships");
    }

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
        session.purgeDatabase();
    }

    @Test
    public void shouldSaveFromStartObjectSetsAllObjectIds() {

        Person mike = new Person("Mike");
        Person dave = new Person("Dave");

        // could use addFriend(...) but hey
        dave.getFriends().add(new Friendship(dave, mike, 5));

        session.save(dave);

        assertThat(dave.getId()).isNotNull();
        assertThat(mike.getId()).isNotNull();
        assertThat(dave.getFriends().get(0).getId()).isNotNull();
    }

    @Test
    public void shouldSaveAndReloadAllSetsAllObjectIdsAndReferencesCorrectly() {

        Person mike = new Person("Mike");
        Person dave = new Person("Dave");
        dave.getFriends().add(new Friendship(dave, mike, 5));

        session.save(dave);

        Collection<Person> personList = session.loadAll(Person.class);

        int expected = 2;
        assertThat(personList.size()).isEqualTo(expected);
        for (Person person : personList) {
            if (person.getName().equals("Dave")) {
                expected--;
                assertThat(person.getFriends().get(0).getFriend().getName()).isEqualTo("Mike");
            } else if (person.getName().equals("Mike")) {
                expected--;
                assertThat(person.getFriends().get(0).getPerson().getName()).isEqualTo("Dave");
            }
        }
        assertThat(expected).isEqualTo(0);
    }

    @Test
    public void shouldSaveFromRelationshipEntitySetsAllObjectIds() {

        Person mike = new Person("Mike");
        Person dave = new Person("Dave");

        Friendship friendship = new Friendship(dave, mike, 5);
        dave.getFriends().add(friendship);

        session.save(friendship);

        assertThat(dave.getId()).isNotNull();
        assertThat(mike.getId()).isNotNull();
        assertThat(dave.getFriends().get(0).getId()).isNotNull();
    }

    @Test
    public void shouldLoadStartObjectHydratesProperly() {

        Person mike = new Person("Mike");
        Person dave = new Person("Dave");
        Friendship friendship = new Friendship(dave, mike, 5);
        dave.getFriends().add(friendship);

        session.save(dave);

        Person daveCopy = session.load(Person.class, dave.getId());
        Friendship friendshipCopy = daveCopy.getFriends().get(0);
        Person mikeCopy = friendshipCopy.getFriend();

        assertThat(daveCopy.getId()).isNotNull();
        assertThat(mikeCopy.getId()).isNotNull();
        assertThat(friendshipCopy.getId()).isNotNull();

        assertThat(daveCopy.getName()).isEqualTo("Dave");
        assertThat(mikeCopy.getName()).isEqualTo("Mike");
        assertThat(friendshipCopy.getStrength()).isEqualTo(5);
    }

    @Test
    public void shouldLoadRelationshipEntityObjectHydratesProperly() {

        Person mike = new Person("Mike");
        Person dave = new Person("Dave");
        Friendship friendship = new Friendship(dave, mike, 5);
        dave.getFriends().add(friendship);

        session.save(dave);

        Friendship friendshipCopy = session.load(Friendship.class, friendship.getId());
        Person daveCopy = friendshipCopy.getPerson();
        Person mikeCopy = friendshipCopy.getFriend();

        assertThat(daveCopy.getId()).isNotNull();
        assertThat(mikeCopy.getId()).isNotNull();
        assertThat(friendshipCopy.getId()).isNotNull();

        assertThat(daveCopy.getName()).isEqualTo("Dave");
        assertThat(mikeCopy.getName()).isEqualTo("Mike");
        assertThat(friendshipCopy.getStrength()).isEqualTo(5);
    }

    /**
     * @see DATAGRAPH-644
     */
    @Test
    public void shouldRetrieveRelationshipEntitySetPropertyCorrectly() {

        Person mike = new Person("Mike");
        Person dave = new Person("Dave");

        Set<String> hobbies = new HashSet<>();
        hobbies.add("Swimming");
        hobbies.add("Cooking");
        dave.getFriends().add(new Friendship(dave, mike, 5, hobbies));

        session.save(dave);

        assertThat(dave.getId()).isNotNull();
        assertThat(mike.getId()).isNotNull();
        assertThat(dave.getFriends().get(0).getId()).isNotNull();

        session.clear();

        mike = session.load(Person.class, mike.getId());
        assertThat(mike.getFriends().get(0).getSharedHobbies()).hasSize(2);

    }
}
