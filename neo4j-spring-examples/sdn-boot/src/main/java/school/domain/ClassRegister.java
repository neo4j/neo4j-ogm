package school.domain;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

/**
 * The class register object connects a teacher
 * with a subject and the pupils who are taught the subject by the teacher
 */
@NodeEntity(label="CLASS")
public class ClassRegister extends Entity {

    private String name;

    @Relationship(type= "SUBJECT_TAUGHT")
    private Subject subject;

    @Relationship(type= "TEACHES_CLASS", direction=Relationship.INCOMING)
    private Teacher teacher;

    @Relationship(type= "ENROLLED", direction=Relationship.INCOMING)
    private Set<Student> students = new HashSet<>();

    public Teacher getTeacher() {
        return teacher;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }

    @Override
    public String toString() {
        return "ClassRegister{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", teacher=" + teacher +
                ", subject=" + subject +
                ", students=" + students.size() +
                '}';
    }
}
