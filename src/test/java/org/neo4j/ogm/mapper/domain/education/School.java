package org.neo4j.ogm.mapper.domain.education;

import java.util.List;

public class School {

    List<Teacher> teachers;

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<Teacher> teachers) {
        this.teachers = teachers;
    }
}
