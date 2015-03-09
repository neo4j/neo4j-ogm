package org.neo4j.ogm.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.Actor;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.WrappingServerIntegrationTest;

public class SessionCypherQueryTest extends WrappingServerIntegrationTest {

    private Session session;

    @Before
    public void setUpSession() {
        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated");
        session = sessionFactory.openSession(baseNeoUrl());
    }

    @Test
    public void shouldQueryForSpecificObjectUsingBespokeParameterisedCypherQuery() {
        this.session.save(new Actor("Alec Baldwin"));
        this.session.save(new Actor("Helen Mirren"));
        this.session.save(new Actor("Matt Damon"));

        Actor loadedActor = this.session.queryForObject(Actor.class, "MATCH (a:Actor) WHERE a.name={param} RETURN a",
                Collections.singletonMap("param", "Alec Baldwin"));
        assertNotNull("The entity wasn't loaded", loadedActor);
        assertEquals("Alec Baldwin", loadedActor.getName());
    }

    @Test
    public void shouldQueryForObjectCollectionUsingBespokeCypherQuery() {
        this.session.save(new Actor("Jeff"));
        this.session.save(new Actor("John"));
        this.session.save(new Actor("Colin"));

        Iterable<Actor> actors = this.session.query(Actor.class, "MATCH (a:Actor) WHERE a.name=~'J.*' RETURN a",
                Collections.<String, Object>emptyMap());
        assertNotNull("The entities weren't loaded", actors);
        assertTrue("The entity wasn't loaded", actors.iterator().hasNext());
        for (Actor actor : actors) {
            assertTrue("Shouldn't've loaded " + actor.getName(),
                    actor.getName().equals("John") || actor.getName().equals("Jeff"));
        }
    }

}
