package org.neo4j.ogm.mapper.domain.collection;

import java.util.List;

public class Parent {

    private List<Child> children;
    private String name;
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
