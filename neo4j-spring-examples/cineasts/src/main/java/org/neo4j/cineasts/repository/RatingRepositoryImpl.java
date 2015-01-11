package org.neo4j.cineasts.repository;

import org.neo4j.cineasts.domain.Movie;
import org.neo4j.cineasts.domain.Rating;
import org.neo4j.cineasts.domain.User;
import org.springframework.stereotype.Repository;

/**
 * temporary until @Query is implemented (todo)
 */
@Repository
public class RatingRepositoryImpl implements RatingRepository {

    @Override
    public Rating findRating(User user, Movie movie) {
        for (Rating rating : user.getRatings()) {
            if (rating.getMovie().equals(movie)) {
                return rating;
            }
        }

        return null;
    }
}
