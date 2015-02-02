package school.domain;

import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity(label = "StudyBuddy")
public class StudyBuddy extends Entity {

    private Student buddyOne;
    private Student buddyTwo;
    private Course course;

    public StudyBuddy(){}

    public StudyBuddy(Student buddyOne, Student buddyTwo, Course course) {
        this.buddyOne = buddyOne;
        this.buddyTwo = buddyTwo;
        this.course = course;
    }

    public void setCourse( Course course )
    {
        this.course = course;
    }

    public void setBuddyTwo( Student buddyTwo )
    {
        this.buddyTwo = buddyTwo;
    }

    public void setBuddyOne( Student buddyOne )
    {
        this.buddyOne = buddyOne;
    }

    public Course getCourse()
    {
        return course;
    }

    public Student getBuddyTwo()
    {
        return buddyTwo;
    }

    public Student getBuddyOne()
    {
        return buddyOne;
    }
}
