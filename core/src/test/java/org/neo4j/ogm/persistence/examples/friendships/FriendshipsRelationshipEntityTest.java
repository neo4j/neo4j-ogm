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
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
public class FriendshipsRelationshipEntityTest extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() throws IOException {
        sessionFactory = new SessionFactory(baseConfiguration, "org.neo4j.ogm.domain.friendships");
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

        assertNotNull(dave.getId());
        assertNotNull(mike.getId());
        assertNotNull(dave.getFriends().get(0).getId());
    }

    @Test
    public void shouldSaveAndReloadAllSetsAllObjectIdsAndReferencesCorrectly() {

        Person mike = new Person("Mike");
        Person dave = new Person("Dave");
        dave.getFriends().add(new Friendship(dave, mike, 5));

        session.save(dave);

        Collection<Person> personList = session.loadAll(Person.class);

        int expected = 2;
        assertEquals(expected, personList.size());
        for (Person person : personList) {
            if (person.getName().equals("Dave")) {
                expected--;
                assertEquals("Mike", person.getFriends().get(0).getFriend().getName());
            } else if (person.getName().equals("Mike")) {
                expected--;
                assertEquals("Dave", person.getFriends().get(0).getPerson().getName());
            }
        }
        assertEquals(0, expected);
    }

    @Test
    public void shouldSaveFromRelationshipEntitySetsAllObjectIds() {

        Person mike = new Person("Mike");
        Person dave = new Person("Dave");

        Friendship friendship = new Friendship(dave, mike, 5);
        dave.getFriends().add(friendship);

        session.save(friendship);

        assertNotNull(dave.getId());
        assertNotNull(mike.getId());
        assertNotNull(dave.getFriends().get(0).getId());
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

        assertNotNull(daveCopy.getId());
        assertNotNull(mikeCopy.getId());
        assertNotNull(friendshipCopy.getId());

        assertEquals("Dave", daveCopy.getName());
        assertEquals("Mike", mikeCopy.getName());
        assertEquals(5, friendshipCopy.getStrength());
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

        assertNotNull(daveCopy.getId());
        assertNotNull(mikeCopy.getId());
        assertNotNull(friendshipCopy.getId());

        assertEquals("Dave", daveCopy.getName());
        assertEquals("Mike", mikeCopy.getName());
        assertEquals(5, friendshipCopy.getStrength());
    }

    /**
     * @see DATAGRAPH-644
     */
    @Test
    public void shouldRetrieveRelationshipEntitySetPropertyCorrecly() {

        Person mike = new Person("Mike");
        Person dave = new Person("Dave");

        Set<String> hobbies = new HashSet<>();
        hobbies.add("Swimming");
        hobbies.add("Cooking");
        dave.getFriends().add(new Friendship(dave, mike, 5, hobbies));

        session.save(dave);

        assertNotNull(dave.getId());
        assertNotNull(mike.getId());
        assertNotNull(dave.getFriends().get(0).getId());

        session.clear();

        mike = session.load(Person.class, mike.getId());
        assertEquals(2, mike.getFriends().get(0).getSharedHobbies().size());
    }
}
