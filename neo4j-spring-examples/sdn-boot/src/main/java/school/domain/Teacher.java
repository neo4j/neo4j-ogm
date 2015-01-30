package school.domain;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

public class Teacher extends Entity {

    private String name;

    @Relationship(type="TEACHES_CLASS")
    private Set<ClassRegister> classRegisters;

    @Relationship(type="DEPARTMENT_MEMBER", direction = Relationship.INCOMING)
    private Department department;

    @Relationship(type="TAUGHT_BY", direction = Relationship.INCOMING)
    private Set<Subject> subjects;

    public Teacher(String name) {
        this();
        this.name = name;
    }

    public Teacher() {
        this.classRegisters = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<ClassRegister> getClassRegisters() {
        return classRegisters;
    }

    public void setClassRegisters(Set<ClassRegister> classRegisters) {
        this.classRegisters = classRegisters;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Set<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(Set<Subject> subjects) {
        this.subjects = subjects;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", classRegisters=" + classRegisters.size() +
                ", department=" + department +
                '}';
    }
}
