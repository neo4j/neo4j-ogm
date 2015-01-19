package org.neo4j.ogm.domain.education;

import java.util.HashSet;
import java.util.Set;

public class School extends DomainObject {

    private Set<Teacher> teachers = new HashSet<>();
    private String name;

    public School() {}

    public School(String name) {
        setName(name);
    }

    public Set<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(Iterable<Teacher> teachers) {
        for (Teacher teacher : teachers) {
            if (!this.teachers.contains(teacher)) {
                teacher.setSchool(this);
                this.teachers.add(teacher);
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
