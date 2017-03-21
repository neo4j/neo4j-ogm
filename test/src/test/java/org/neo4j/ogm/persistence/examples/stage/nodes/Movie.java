package org.neo4j.ogm.persistence.examples.stage.nodes;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class Movie extends BaseNode {

    public Movie() {
        super();
    }

    public Movie(String title) {
        super(title);
    }
}
