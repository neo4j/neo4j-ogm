package org.neo4j.ogm.domain.abstraction;

public abstract class AnotherEntity extends Entity {

    protected String anotherValue;

    public AnotherEntity() {
        super();
    }

    public AnotherEntity(String uuid) {
        super(uuid);
    }

    public String getAnotherValue() {
        return anotherValue;
    }

    public void setAnotherValue(String anotherValue) {
        this.anotherValue = anotherValue;
    }

}
