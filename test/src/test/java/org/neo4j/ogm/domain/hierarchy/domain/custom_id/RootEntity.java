package org.neo4j.ogm.domain.hierarchy.domain.custom_id;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public abstract class RootEntity extends MostBasicEntity {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
