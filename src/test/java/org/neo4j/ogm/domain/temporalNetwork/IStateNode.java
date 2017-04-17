package org.neo4j.ogm.domain.temporalNetwork;

public interface IStateNode<R extends ITimeRelation> extends INode {
    R getIdentityRelation();

    void setIdentityRelation(R value);

}