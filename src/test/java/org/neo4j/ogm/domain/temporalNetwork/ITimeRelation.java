package org.neo4j.ogm.domain.temporalNetwork;

public interface ITimeRelation<S, T> {
    Long getRelationshipId();

    void setRelationshipId(Long relationshipId);

    S getSourceNode();

    void setSourceNode(S identityNode);

    T getTargetNode();

    void setTargetNode(T stateNode);

    Long getFrom();

    void setFrom(Long from);

    Long getTo();

    void setTo(Long to);
}