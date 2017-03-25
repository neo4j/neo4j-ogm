package org.neo4j.ogm.persistence.examples.stage.nodes;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class BaseNode {

    @GraphId
    public Long id;

    public String title;

    public BaseNode() {

    }

    public BaseNode(String title) {
        this.title = title;
    }
}
