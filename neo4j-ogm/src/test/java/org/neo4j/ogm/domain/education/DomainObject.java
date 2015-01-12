package org.neo4j.ogm.domain.education;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public abstract class DomainObject {

    Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
