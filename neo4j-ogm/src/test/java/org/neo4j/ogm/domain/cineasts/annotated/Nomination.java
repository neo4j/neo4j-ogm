package org.neo4j.ogm.domain.cineasts.annotated;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity
public class Nomination {

    Long id;
    @EndNode
    Movie movie;
    @StartNode
    Actor actor;
    String name;
    int year;

    public Nomination(Movie movie, Actor actor, String name, int year) {
        this.movie = movie;
        this.actor = actor;
        this.name = name;
        this.year = year;
    }
}
