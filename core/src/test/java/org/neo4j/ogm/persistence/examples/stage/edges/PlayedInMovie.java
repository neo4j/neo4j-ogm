package org.neo4j.ogm.persistence.examples.stage.edges;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.persistence.examples.stage.nodes.FilmActor;
import org.neo4j.ogm.persistence.examples.stage.nodes.Movie;

@RelationshipEntity(type="PLAYED_IN")
public class PlayedInMovie extends BaseEdge<FilmActor, Movie>{

    public PlayedInMovie() {
        super();
    }

    public PlayedInMovie(FilmActor start, Movie end, String title) {
        super(start, end, title);
    }

}
