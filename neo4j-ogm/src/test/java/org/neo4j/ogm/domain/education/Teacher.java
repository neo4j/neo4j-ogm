package org.neo4j.ogm.domain.education;

import java.util.List;

public class Teacher {

    private String name;
    private List<Course> courses;
    private Long id;
    private School school;

    public Teacher() {}

    public Teacher(String name) {
        setName(name);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // @Lazy
    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        // persistable?
        this.courses = courses;
    }

    public School getSchool() {
        return school;
    }

    public void setSchool(School school) {
        if (!school.getTeachers().contains(this)) {
            school.getTeachers().add(this);
        }
        this.school = school;
    }

}
