package org.neo4j.ogm.domain.education;

public class Student extends DomainObject {

    private String name;

    public Student() {}

    public Student(String name) {
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
