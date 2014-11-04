package org.neo4j.ogm.mapper.domain.cineasts;

import java.util.Set;

public class User {

    String login;
    String name;
    String password;

    Set<Rating> ratings;
    Set<User> friends;

    Rating rate(Movie movie, int stars, String comment) {
        return null;
    }

    void befriend(User user) {
    }
}
