package org.neo4j.ogm.mapper.domain.collection;

import java.util.List;

public class Parent extends DomainObject {

    private List<Child> children;

    private String name;

    public List<Child> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChildren(List<Child> children) {
        this.children = children;
    }
}
