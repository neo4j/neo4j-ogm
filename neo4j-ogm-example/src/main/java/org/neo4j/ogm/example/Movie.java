package org.neo4j.ogm.example;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

@NodeEntity("Movie")
public class Movie {
    @Id
    String title;

    String tagline;

    @Relationship(type = "ACTED_IN", direction = Relationship.Direction.INCOMING)
    List<Actor> actors;

    @Relationship(type = "DIRECTED", direction = Relationship.Direction.INCOMING)
    List<Person> directors;

    @Relationship(type = "REVIEWED", direction = Relationship.Direction.INCOMING)
    List<Reviewer> reviewers;

    @Override public String toString() {
        return "Movie{" +
            "title='" + title + '\'' +
            ", actors=" + actors +
            ", directors=" + directors +
            ", reviewers=" + reviewers +
            '}';
    }

    public Movie withTagline(String newTagline) {
        Movie movie = new Movie();
        movie.title = this.title;
        movie.actors = this.actors;
        movie.directors = this.directors;
        movie.reviewers = reviewers;
        movie.tagline = newTagline;
        return movie;
    }
}
