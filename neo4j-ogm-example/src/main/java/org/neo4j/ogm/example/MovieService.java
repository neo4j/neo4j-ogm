/*
 * Copyright (c) 2002-2025 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
