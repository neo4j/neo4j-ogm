package org.neo4j.ogm.index;

import static org.junit.Assert.*;

import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.domain.cineasts.partial.Actor;
import org.neo4j.ogm.index.domain.valid.Invoice;
import org.neo4j.ogm.session.Neo4jException;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

/**
 * @author Mark Angrish
 */
public class LookupByPrimaryIndexTests extends MultiDriverTestClass {

    @Test
    public void loadUsesPrimaryIndexWhenPresent() {

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated");
        final Session session = sessionFactory.openSession();

        User user1 = new User("login1", "Name 1", "password");
        session.save(user1);

        final Session session2 = sessionFactory.openSession();

        final User retrievedUser1 = session2.load(User.class, "login1");
        assertNotNull(retrievedUser1);
        assertEquals(user1.getLogin(), retrievedUser1.getLogin());
    }

    @Test
    public void loadUsesGraphIdWhenPrimaryIndexNotPresent() {

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.partial");
        final Session session = sessionFactory.openSession();

        Actor actor = new Actor("David Hasslehoff");
        session.save(actor);

        final Long id = actor.getId();

        final Session session2 = sessionFactory.openSession();

        final Actor retrievedActor = session2.load(Actor.class, id);
        assertNotNull(retrievedActor);
        assertEquals(actor.getName(), retrievedActor.getName());

    }

    @Test(expected = Neo4jException.class)
    public void exceptionRaisedWhenLookupIsDoneWithGraphIdAndThereIsAPrimaryIndexPresent() {

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated");
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

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.index.domain.valid");
        final Session session = sessionFactory.openSession();

        Invoice invoice = new Invoice(223L, "Company", 100000L);
        session.save(invoice);

        final Session session2 = sessionFactory.openSession();

        Invoice  retrievedInvoice = session2.load(Invoice.class, 223L);
        assertNotNull(retrievedInvoice);
        assertEquals(invoice.getId(), retrievedInvoice.getId());
    }
}
