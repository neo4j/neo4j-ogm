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

    @Relationship(type= "TEACHES") // outgoing, should be incoming or "taught-by"
    private Teacher teacher;

    @Relationship(type= "SUBJECT")
    private Subject subject;

    private Set<Student> students = new HashSet<>();

    @Relationship(type= "TEACHES")
    public Teacher getTeacher() {
        return teacher;
    }

    @Relationship(type= "TEACHES")
    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Relationship(type= "TEACHES")
    public Subject getSubject() {
        return subject;
    }

    @Relationship(type= "TEACHES")
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Relationship(type="ENROLLED")
    public Set<Student> getStudents() {
        return students;
    }

    @Relationship(type="ENROLLED")
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
