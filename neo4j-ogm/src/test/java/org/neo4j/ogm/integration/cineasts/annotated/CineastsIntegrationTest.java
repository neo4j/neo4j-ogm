package org.neo4j.ogm.integration.cineasts.annotated;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.domain.cineasts.annotated.Movie;
import org.neo4j.ogm.domain.cineasts.annotated.Rating;
import org.neo4j.ogm.domain.satellites.Program;
import org.neo4j.ogm.domain.satellites.Satellite;
import org.neo4j.ogm.integration.IntegrationTest;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.transaction.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Ignore
public class CineastsIntegrationTest extends IntegrationTest {

    @BeforeClass
    public static void init() throws IOException {
        setUp();
        session = new SessionFactory("org.neo4j.ogm.domain.cineasts.annotated").openSession("http://localhost:" + neoPort);
        importCineasts();

    }

    @Test
    public void loadMovies() {
        Collection<Movie> movies = session.loadAll(Movie.class);

        assertEquals(3, movies.size());

        for (Movie movie : movies) {

            System.out.println("Movie:" + movie.getTitle());

            for (Rating rating : movie.getRatings()) {
                System.out.println("\trating:" + rating.getMovie());
                System.out.println("\t\tcomment:" + rating.getComment());
            }
        }

    }

    private static void importCineasts() {
        session.execute(load("org/neo4j/ogm/cql/cineasts.cql"));
    }
}
