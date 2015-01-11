package org.neo4j.cineasts.domain;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

public class Director extends Person {
    public Director(String id, String name) {
        super(id, name);
    }

    public Director() {
    }

    @Relationship(type = "DIRECTED")
    private Set<Movie> directedMovies=new HashSet<Movie>();

    public Director(String id) {
        super(id,null);
    }

    public Set<Movie> getDirectedMovies() {
        return directedMovies;
    }

    public void directed(Movie movie) {
        this.directedMovies.add(movie);
    }

}
