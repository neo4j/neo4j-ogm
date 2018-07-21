package org.neo4j.ogm.domain.abstraction;

import java.util.HashSet;
import java.util.Set;

public class ChildA extends Entity {

    private String value;

    private Set<AnotherEntity> children = new HashSet<>();

    public ChildA() {
        super();
    }

    public ChildA(String uuid) {
        super(uuid);
    }

    public ChildA add(AnotherEntity childB) {
        children.add(childB);
        return this;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Set<AnotherEntity> getChildren() {
        return children;
    }

    public void setChildren(Set<AnotherEntity> children) {
        this.children = children;
    }
}
