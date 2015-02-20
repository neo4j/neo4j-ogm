package org.neo4j.ogm.domain.cineasts.partial;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity
public class Role {

    Long id;
    String played;

    @StartNode
    Actor actor;

    @EndNode
    Movie movie;

    public Role() {}

    public Role(String character, Actor actor, Movie movie) {
        played = character;
        this.actor = actor;
        this.movie = movie;
    }


}
