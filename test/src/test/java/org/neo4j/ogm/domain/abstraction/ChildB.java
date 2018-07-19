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
        value = uuid.hashCode();
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

}
