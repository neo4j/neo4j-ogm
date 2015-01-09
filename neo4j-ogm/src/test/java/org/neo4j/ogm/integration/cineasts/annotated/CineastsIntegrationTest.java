package org.neo4j.ogm.integration.cineasts.annotated;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Rating;
import org.neo4j.ogm.domain.cineasts.annotated.User;
import org.neo4j.ogm.integration.IntegrationTest;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.session.SessionFactory;

/**
 * Simple integration test based on cineasts that exercises relationship entities.
 */
public class CineastsIntegrationTest extends IntegrationTest {

    @BeforeClass
    public static void init() throws IOException {
        setUp();
        session = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated").openSession("http://localhost:" + neoPort);
        importCineasts();
    }

    private static void importCineasts() {
        session.execute(load("org/neo4j/ogm/cql/cineasts.cql"));
    }

    @Test
    public void loadRatingsAndCommentsAboutMovies() {
        Collection<Movie> movies = session.loadAll(Movie.class);

        assertEquals(3, movies.size());

        for (Movie movie : movies) {

            System.out.println("Movie: " + movie.getTitle());
            if (movie.getRatings() != null) {
                for (Rating rating : movie.getRatings()) {
                    assertNotNull("The film on the rating shouldn't be null", rating.getMovie());
                    assertSame("The film on the rating was not mapped correctly", movie, rating.getMovie());
                    assertNotNull("The film critic wasn't set", rating.getUser());
                    System.out.println("\trating: " + rating.getMovie());
                    System.out.println("\t\tcomment: " + rating.getComment());
                    System.out.println("\t\tcritic:  " + rating.getUser().getName());
                }
            }
        }
    }

    @Test
    public void loadParticularUserRatingsAndComments() {
        Collection<User> filmCritics = session.loadByProperty(User.class, new Property<String, Object>("name", "Michal"));
        assertEquals(1, filmCritics.size());

        User critic = filmCritics.iterator().next();
        assertEquals(2, critic.getRatings().size());

        for (Rating rating : critic.getRatings()) {
            assertNotNull("The comment should've been mapped", rating.getComment());
            assertTrue("The star rating should've been mapped", rating.getStars() > 0);
            assertNotNull("The user start node should've been mapped", rating.getUser());
            assertNotNull("The movie end node should've been mapped", rating.getMovie());
        }
    }

    @Test
    public void loadRatingsForSpecificFilm() {
        Collection<Movie> films = session.loadByProperty(Movie.class, new Property<String, Object>("title", "Top Gear"));
        assertEquals(1, films.size());

        Movie film = films.iterator().next();
        assertEquals(2, film.getRatings().size());

        for (Rating rating : film.getRatings()) {
            assertTrue("The star rating should've been mapped", rating.getStars() > 0);
            assertNotNull("The user start node should've been mapped", rating.getUser());
            assertSame("The wrong film was mapped to the rating", film, rating.getMovie());
        }
    }

}
