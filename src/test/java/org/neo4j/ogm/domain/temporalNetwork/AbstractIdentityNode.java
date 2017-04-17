package org.neo4j.ogm.domain.temporalNetwork;

import org.neo4j.ogm.annotation.GraphId;

import java.util.Set;

public abstract class AbstractIdentityNode<R extends ITimeRelation> implements IIdentityNode {
    @GraphId
    protected Long id;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public abstract Set<R> getStates();

    public abstract void setStates(Set<R> states);
}
