package org.neo4j.ogm.domain.rulers;

import org.neo4j.ogm.annotation.Index;

import java.util.List;

public abstract class Person {

    protected List<Person> heirs;

    @Index
    protected String name;

    public abstract String sex();

    public abstract boolean isCommoner();

    public List<Person> getHeirs() {
        return heirs;
    }

    public void setHeirs(List<Person> heirs) {
        this.heirs = heirs;
    }

}
