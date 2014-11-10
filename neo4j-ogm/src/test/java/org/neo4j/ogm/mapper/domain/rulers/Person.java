package org.neo4j.ogm.mapper.domain.rulers;

import java.util.List;

public abstract class Person {

    protected List<Person> heirs;

    public abstract String sex();

    public abstract boolean isCommoner();

    public List<Person> getHeirs() {
        return heirs;
    }

    public void setHeirs(List<Person> heirs) {
        this.heirs = heirs;
    }

}
