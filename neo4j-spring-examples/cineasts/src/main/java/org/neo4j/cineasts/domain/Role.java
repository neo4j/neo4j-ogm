package org.neo4j.cineasts.domain;

import org.neo4j.ogm.annotation.GraphId;

@RelationshipEntity(type = "ACTS_IN")
public class Role {
    @GraphId
    Long id;
    @EndNode Movie movie;
    @StartNode Actor actor;

    String name;

    public Role() {
    }

    public Role(Actor actor, Movie movie, String roleName) {
        this.movie = movie;
        this.actor = actor;
        this.name = roleName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Movie getMovie() {
        return movie;
    }

    public Actor getActor() {
        return actor;
    }

    @Override
    public String toString() {
        return String.format("%s acts as %s in %s", actor, name, movie);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;
        if (id == null) return super.equals(o);
        return id.equals(role.id);

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : super.hashCode();
    }

}
