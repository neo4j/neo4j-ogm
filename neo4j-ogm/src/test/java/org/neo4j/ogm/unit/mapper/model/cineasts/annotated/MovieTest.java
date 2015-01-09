package org.neo4j.ogm.unit.mapper.model.cineasts.annotated;

import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.domain.bike.Bike;
import org.neo4j.ogm.domain.bike.Wheel;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.unit.mapper.model.bike.BikeRequest;

import java.util.Collection;

import static org.junit.Assert.*;

public class MovieTest {

    @Test
    @Ignore
    public void testDeserialiseMovie() {

        MovieRequest movieRequest = new MovieRequest();

        SessionFactory sessionFactory = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated");
        Neo4jSession session = ((Neo4jSession) sessionFactory.openSession("dummy-url"));
        session.setRequest(movieRequest);

        Movie movie = session.load(Movie.class, 15L, 1);

        assertEquals("Pulp Fiction", movie.getTitle());
        assertNotNull(movie.getRatings());
        assertEquals(2, movie.getRatings().size());
    }
}
