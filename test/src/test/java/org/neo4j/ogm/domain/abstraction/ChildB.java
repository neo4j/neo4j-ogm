package org.neo4j.ogm.domain.abstraction;

public class ChildB extends AnotherEntity {

    private Integer value;

    public ChildB() {
        super();
    }

    public ChildB(String uuid) {
        super(uuid);
    }

    @Override
    public void postLoad() {
        // FIXME - #414 - @PostLoad is not called in child overrided method (this method does not execute)
        value = uuid.hashCode();
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

}
