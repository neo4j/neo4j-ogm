package org.neo4j.ogm.domain.temporalNetwork;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.StartNode;

public abstract class AbstractTimeRelation<S extends INode, T extends INode> implements ITimeRelation<S, T> {
    @GraphId
    private Long relationshipId;

    @StartNode
    private S sourceNode;

    @EndNode
    private T targetNode;

    @Property
    private Long from;

    @Property
    private Long to;

    public Long getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(Long relationshipId) {
        this.relationshipId = relationshipId;
    }

    @Override public S getSourceNode() {
        return sourceNode;
    }

    @Override public void setSourceNode(S sourceNode) {
        this.sourceNode = sourceNode;
    }

    @Override public T getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(T targetNode) {
        this.targetNode = targetNode;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }
}
