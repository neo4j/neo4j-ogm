package org.neo4j.cineasts.domain;

import org.neo4j.ogm.annotation.Relationship;

import java.util.Collection;

public class Actor extends Person {
    public Actor(String id, String name) {
        super(id, name);
    }

    public Actor() {
    }

    @Relationship
    Collection<Role> roles;

    public Actor(String id) {
        super(id,null);
    }

    public Iterable<Role> getRoles() {
        return roles;
    }

    public Role playedIn(Movie movie, String roleName) {
        final Role role = new Role(this, movie, roleName);
        roles.add(role);
        return role;
    }
}
