package school.domain;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;


@RelationshipEntity(type="STUDY_BUDDY")
public class StudyBuddy extends Entity {

    @StartNode
    private Student buddyOne;

    @EndNode
    private Student buddyTwo;

    private Subject subject;

    public StudyBuddy(Student buddyOne, Student buddyTwo, Subject subject) {
        this.buddyOne = buddyOne;
        this.buddyTwo = buddyTwo;
        this.subject = subject;
    }

    public Student getBuddy(Student student) {
        return student == buddyOne ? buddyTwo : student == buddyTwo ? buddyOne : null;
    }

    public Subject getSubject() {
        return subject;
    }

    @Override
    public String toString() {
        return "StudyBuddy{" +
                "id=" + getId() +
                ", buddyOne=" + buddyOne +
                ", buddyTwo=" + buddyTwo +
                ", subject=" + subject +
                '}';
    }
}
