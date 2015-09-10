package org.neo4j.ogm.domain.temporalNetwork;

import org.neo4j.ogm.annotation.GraphId;

public abstract class AbstractStateNode <R extends ITimeRelation> implements IStateNode<R> {
    @GraphId
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public abstract R getIdentityRelation();

    @Override
    public abstract void setIdentityRelation(R identityRelation);
}
