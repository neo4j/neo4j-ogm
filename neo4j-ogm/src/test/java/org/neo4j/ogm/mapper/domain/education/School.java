package org.neo4j.ogm.mapper.domain.education;

import java.util.ArrayList;
import java.util.List;

public class School extends DomainObject {

    private List<Teacher> teachers = new ArrayList<>();

    public List<Teacher> getTeachers() {
        return teachers;
    }

    public void setTeachers(List<Teacher> teachers) {
        for (Teacher teacher : teachers) {
            teacher.setSchool(this);
        }
        this.teachers = teachers;
    }

}
