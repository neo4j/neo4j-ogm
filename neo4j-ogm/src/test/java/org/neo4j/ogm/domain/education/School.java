package org.neo4j.ogm.domain.education;

import java.util.ArrayList;
import java.util.List;

public class School extends DomainObject {

    private List<Teacher> teachers = new ArrayList<>();
    private String name;

    public School() {}

    public School(String name) {
        setName(name);
    }

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<Teacher> teachers) {
        for (Teacher teacher : teachers) {
            teacher.setSchool(this);
        }
        this.teachers = teachers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
