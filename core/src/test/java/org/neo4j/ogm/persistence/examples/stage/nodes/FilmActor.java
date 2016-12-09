package org.neo4j.ogm.persistence.examples.stage.nodes;


import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.persistence.examples.stage.edges.LastMovie;
import org.neo4j.ogm.persistence.examples.stage.edges.PlayedInMovie;

@NodeEntity
public class FilmActor extends BaseNode {

    public FilmActor() {
        super();
    }

    public FilmActor(String title) {
        super(title);
    }

    @Relationship(type = "PLAYED_IN")
    public Set<PlayedInMovie> movies = new HashSet<>();

    @Relationship(type = "LAST_APPEARENCE")
    public LastMovie lastMovie;
}
