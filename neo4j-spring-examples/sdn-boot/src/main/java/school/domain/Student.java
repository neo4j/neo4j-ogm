package school.domain;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;


public class Student extends Entity {

    private String name;

    @Relationship(type = "ENROLLED")
    private Set<ClassRegister> classRegisters;

    @Relationship(type="STUDY_BUDDY", direction=Relationship.UNDIRECTED)
    private Set<StudyBuddy> studyBuddies;

    public Student() {
        this.studyBuddies = new HashSet<>();
        this.classRegisters = new HashSet<>();
    }

    public Student(String name) {
        this();
        this.name = name;
    }

    public void addStudyBuddy(Student buddy, Subject subject) {
        studyBuddies.add(new StudyBuddy(this, buddy, subject));
    }

    public String getName() {
        return name;
    }

    public Set<StudyBuddy> getStudyBuddies() {
        return studyBuddies;
    }

    public void setName(String name) {
        this.name = name;
    }

    //@JsonIgnore
    //@Relationship(type = "ENROLLED", direction=Relationship.INCOMING)
    public Set<ClassRegister> getClassRegisters() {
        return classRegisters;
    }

    //@JsonIgnore
    //@Relationship(type = "ENROLLED", direction=Relationship.INCOMING)
    public void setClassRegisters(Set<ClassRegister> classRegisters) {
        this.classRegisters = classRegisters;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", classRegisters=" + classRegisters.size() +
                ", studyBuddies=" + studyBuddies.size() +
                '}';
    }
}
