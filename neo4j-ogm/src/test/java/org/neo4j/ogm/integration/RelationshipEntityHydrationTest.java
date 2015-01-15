package org.neo4j.ogm.integration;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.friendships.Friendship;
import org.neo4j.ogm.domain.friendships.Person;
import org.neo4j.ogm.session.SessionFactory;

import java.io.IOException;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

public class RelationshipEntityHydrationTest extends IntegrationTest {

    private static SessionFactory sessionFactory;

    @Before
    public void init() throws IOException {
        setUp();
        sessionFactory = new SessionFactory("org.neo4j.ogm.domain.friendship");
        session = sessionFactory.openSession("http://localhost:" + neoPort);
    }

    @Test
    public void testThatSaveFromStartObjectSetsAllObjectIds() {

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
    public void testThatSaveFromRelationshipEntitySetsAllObjectIds() {

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
    public void testThatLoadStartObjectHydratesProperly() {

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
    public void testThatLoadRelationshipEntityObjectHydratesProperly() {

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

}
