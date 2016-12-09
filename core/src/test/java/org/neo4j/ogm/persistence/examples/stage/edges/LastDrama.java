package org.neo4j.ogm.persistence.examples.stage.edges;

import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.persistence.examples.stage.nodes.Drama;
import org.neo4j.ogm.persistence.examples.stage.nodes.StageActor;

@RelationshipEntity(type = "LAST_APPEARENCE")
public class LastDrama extends BaseEdge<StageActor, Drama> {

    public LastDrama() {
        super();
    }

    public LastDrama(StageActor start, Drama end, String title) {
        super(start, end, title);
    }
}
