package org.neo4j.ogm.domain.cineasts.annotated;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

public class Actor {

    private Long id;
    private String name;
    private Set<Movie> filmography;

    @Relationship(type="ACTS_IN", direction="OUTGOING")
    private Set<Role> roles;

    private Set<Nomination> nominations;

    public Actor(String name) {
        this.name = name;
    }

    public Role playedIn(Movie movie, String roleName) {
        if(roles==null) {
            roles = new HashSet<>();
        }
        Role role = new Role(movie,this,roleName);
        roles.add(role);
        return role;
    }

    public Nomination nominatedFor(Movie movie, String nominationName, int year) {
        if(nominations==null) {
            nominations = new HashSet<>();
        }
        Nomination nomination = new Nomination(movie,this,nominationName,year);
        nominations.add(nomination);
        return nomination;
    }

}
