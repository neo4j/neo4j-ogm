package org.neo4j.ogm.persistence.examples.stage.nodes;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.persistence.examples.stage.edges.LastDrama;
import org.neo4j.ogm.persistence.examples.stage.edges.PlayedInDrama;

@NodeEntity
public class StageActor extends BaseNode {

    public StageActor() {
        super();
    }

    public StageActor(String title) {
        super(title);
    }

    @Relationship(type = "PLAYED_IN")
    public Set<PlayedInDrama> dramas = new HashSet<>();

    @Relationship(type = "LAST_APPEARANCE")
    public LastDrama lastDrama;
}
