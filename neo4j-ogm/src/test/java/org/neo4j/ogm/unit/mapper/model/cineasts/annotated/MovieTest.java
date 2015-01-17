package org.neo4j.ogm.unit.mapper.model.cineasts.annotated;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Rating;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MovieTest {

    @Test
    @Ignore
    // this test won't work for 2 reasons
    // 1. the incoming relationship on Movie is not annotated with direction INCOMING
    // in fact, its not annotated at all.
    // 2. even if it were annotated, right now, we don't "do" INCOMING, for reasons that
    // this margin is too small to explain.
    public void testDeserialiseMovie() {

        MovieRequest movieRequest = new MovieRequest();

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated");
        Neo4jSession session = ((Neo4jSession) sessionFactory.openSession("dummy-url"));
        session.setRequest(movieRequest);

        Movie movie = session.load(Movie.class, 15L, 1);

        assertEquals("Pulp Fiction", movie.getTitle());
        assertNotNull(movie.getRatings());
        assertEquals(1, movie.getRatings().size());

        Rating rating = movie.getRatings().iterator().next();

        assertEquals("Michal", rating.getUser().getName());
        assertEquals("Pulp Fiction", rating.getMovie().getTitle());
    }
}
