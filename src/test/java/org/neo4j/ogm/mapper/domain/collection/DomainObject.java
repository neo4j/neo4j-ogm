package org.neo4j.ogm.mapper.domain.collection;

public abstract class DomainObject
{
    Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
