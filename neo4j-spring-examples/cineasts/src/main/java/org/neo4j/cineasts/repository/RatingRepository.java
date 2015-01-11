package org.neo4j.cineasts.repository;

import org.neo4j.cineasts.domain.Movie;
import org.neo4j.cineasts.domain.Rating;
import org.neo4j.cineasts.domain.User;

//@org.springframework.stereotype.Repository
public interface RatingRepository {// extends Repository<Rating, Long> {

//    @Query("MATCH (u:User)-[r:RATED]->(m:Movie) WHERE id(u)={user} AND id(m)={movie} RETURN r")
    Rating findRating(User user, Movie movie);
}
