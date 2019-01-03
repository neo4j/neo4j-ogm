package org.neo4j.ogm.persistence.examples.stage.edges;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.StartNode;
import org.neo4j.ogm.persistence.examples.stage.nodes.BaseNode;

public abstract class BaseEdge<T extends BaseNode, U extends BaseNode> {

    @Id @GeneratedValue
    public Long relationshipId;

    public String title;

    @StartNode
    public T start;

    @EndNode
    public U end;

    public BaseEdge() {

    }

    public BaseEdge(T start, U end, String title) {
        this.start = start;
        this.end = end;
        this.title = title;
    }
}
