package school.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

public class School extends Entity {

    private String name;

    //@JsonManagedReference("department")
    @Relationship(type = "DEPARTMENT")
    private Set<Department> departments;

    //@JsonManagedReference("staff")
    @Relationship(type = "STAFF")
    private Set<Teacher> teachers;

//    @Relationship(type = "HEAD_TEACHER")
//    private Teacher headTeacher;
//
//    @JsonIgnore
//    @Relationship(type = "DEPUTY_HEAD_TEACHER")
//    private Teacher deputyHeadTeacher;

    //@JsonManagedReference("student")
    @Relationship(type = "STUDENT")
    private Set<Student> students;

    public School() {
        this.departments = new HashSet<>();
        this.teachers = new HashSet<>();
        this.students = new HashSet<>();
    }

    public School(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //@JsonManagedReference("department")
    @Relationship(type = "DEPARTMENT")
    public Set<Department> getDepartments() {
        return departments;
    }

    //@JsonManagedReference("department")
    @Relationship(type = "DEPARTMENT")
    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }

//    public Teacher getHeadTeacher() {
//        return headTeacher;
//    }
//
//    public void setHeadTeacher(Teacher headTeacher) {
//        this.headTeacher = headTeacher;
//    }
//
//    public Teacher getDeputyHeadTeacher() {
//        return deputyHeadTeacher;
//    }
//
//    public void setDeputyHeadTeacher(Teacher deputyHeadTeacher) {
//        this.deputyHeadTeacher = deputyHeadTeacher;
//    }

    //@JsonManagedReference("staff")
    @Relationship(type = "STAFF")
    public Set<Teacher> getTeachers() {
        return teachers;
    }

    //@JsonManagedReference("staff")
    @Relationship(type = "STAFF")
    public void setTeachers(Set<Teacher> teachers) {
        this.teachers = teachers;
    }

    //@JsonManagedReference("student")
    @Relationship(type = "STUDENT")
    public Set<Student> getStudents() {
        return students;
    }

    @JsonManagedReference("student")
    @Relationship(type = "STUDENT")
    public void setStudents(Set<Student> students) {
        this.students = students;
    }

    @Override
    public String toString() {
        return "School{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", departments=" + departments.size() +
                ", teachers=" + teachers.size() +
                ", students=" + students.size() +
                '}';
    }
}

