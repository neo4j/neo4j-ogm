package org.neo4j.ogm.domain.cineasts.annotated;

import java.util.Set;

public class Actor {

    private String id;
    private String name;
    private Set<Movie> filmography;

    public Role playedIn(Movie movie, String role) {
        return null;
    }


}
