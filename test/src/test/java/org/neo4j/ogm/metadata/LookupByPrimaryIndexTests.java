package org.neo4j.ogm.metadata;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.domain.autoindex.valid.Invoice;
import org.neo4j.ogm.domain.cineasts.annotated.ExtendedUser;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.domain.cineasts.partial.Actor;
import org.neo4j.ogm.session.Neo4jException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Mark Angrish
 * @author Nicolas Mervaillie
 */
public class LookupByPrimaryIndexTests extends MultiDriverTestClass {

    private static SessionFactory sessionFactory;
    private Session session;

    @BeforeClass
    public static void oneTimeSetUp() {
        sessionFactory = new SessionFactory(getBaseConfiguration().build(), "org.neo4j.ogm.domain.cineasts.annotated");
    }

    @Before
    public void setUp() {
        session = sessionFactory.openSession();
    }

    @Test
    public void loadUsesPrimaryIndexWhenPresent() {

        User user1 = new User("login1", "Name 1", "password");
        session.save(user1);

        final Session session2 = sessionFactory.openSession();

        final User retrievedUser1 = session2.load(User.class, "login1");
        assertNotNull(retrievedUser1);
        assertEquals(user1.getLogin(), retrievedUser1.getLogin());
    }

    @Test
    public void loadUsesPrimaryIndexWhenPresentOnSuperclass() {

        ExtendedUser user1 = new ExtendedUser("login2", "Name 2", "password");
        session.save(user1);

        final Session session2 = sessionFactory.openSession();

        final User retrievedUser1 = session2.load(ExtendedUser.class, "login2");
        assertNotNull(retrievedUser1);
        assertEquals(user1.getLogin(), retrievedUser1.getLogin());
    }

    @Test
    public void loadUsesGraphIdWhenPrimaryIndexNotPresent() {

        SessionFactory sessionFactory = new SessionFactory(getBaseConfiguration().build(), "org.neo4j.ogm.domain.cineasts.partial");
        Session session1 = sessionFactory.openSession();
        Actor actor = new Actor("David Hasslehoff");
        session1.save(actor);

        final Long id = actor.getId();

        final Session session2 = sessionFactory.openSession();

        final Actor retrievedActor = session2.load(Actor.class, id);
        assertNotNull(retrievedActor);
        assertEquals(actor.getName(), retrievedActor.getName());
    }

    @Test(expected = Neo4jException.class)
    public void exceptionRaisedWhenLookupIsDoneWithGraphIdAndThereIsAPrimaryIndexPresent() {

        final Session session = sessionFactory.openSession();

        User user1 = new User("login1", "Name 1", "password");
        session.save(user1);

        final Session session2 = sessionFactory.openSession();

        session2.load(User.class, user1.getId());
    }

    /**
     * This test makes sure that if the primary key is a Long, it isn't mixed up with the Graph Id.
     */
    @Test
    public void loadUsesPrimaryIndexWhenPresentEvenIfTypeIsLong() {

        SessionFactory sessionFactory = new SessionFactory(getBaseConfiguration().build(), "org.neo4j.ogm.domain.autoindex.valid");
        Session session1 = sessionFactory.openSession();

        Invoice invoice = new Invoice(223L, "Company", 100000L);
        session1.save(invoice);

        final Session session2 = sessionFactory.openSession();

        Invoice retrievedInvoice = session2.load(Invoice.class, 223L);
        assertNotNull(retrievedInvoice);
        assertEquals(invoice.getId(), retrievedInvoice.getId());
    }
}
