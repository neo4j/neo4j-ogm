package org.neo4j.ogm.example;

import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Gerrit Meier
 */
public class MovieService {

    final SessionFactory sessionFactory;

    public MovieService() {
        Configuration config = new Configuration.Builder()
            .uri("neo4j://localhost:7687")
            .credentials("neo4j", "verysecret")
            .build();
        this.sessionFactory = new SessionFactory(config, "org.neo4j.ogm.example");
    }

    Movie findMovieByTitle(String title) {
        Session session = sessionFactory.openSession();
        return Optional.ofNullable(
                session.queryForObject(Movie.class, "MATCH (m:Movie {title:$title}) return m", Map.of("title", title)))
            .orElseThrow(() -> new MovieNotFoundException(title));
    }

    public List<Movie> allMovies() {
        Session session = sessionFactory.openSession();
        return new ArrayList<>(session.loadAll(Movie.class));
    }

    Movie updateTagline(String title, String newTagline) {
        Session session = sessionFactory.openSession();
        Movie movie = session.queryForObject(Movie.class, "MATCH (m:Movie{title:$title}) return m", Map.of("title", title));
        Movie updatedMovie = movie.withTagline(newTagline);
        session.save(updatedMovie);
        return updatedMovie;
    }

    List<MovieRating> getRatings() {
        Session session = sessionFactory.openSession();
        List<MovieRating> ratings = session.queryDto(
            "MATCH (m:Movie)<-[r:REVIEWED]-(p:Person) RETURN m.title as title, avg(r.rating) as rating, collect(p.name) as reviewers",
            Map.of(),
            MovieRating.class);
        return ratings;
    }
}
