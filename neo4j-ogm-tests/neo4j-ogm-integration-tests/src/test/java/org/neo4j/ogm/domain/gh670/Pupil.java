package org.neo4j.ogm.domain.gh670;

import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

public class Pupil extends Person {

    @Relationship(value = "TAUGHT_BY", direction = Relationship.Direction.INCOMING)
    private Teacher taughtBy;

    @Relationship(value = "TAKES")
    private List<Course> coursesTaken;

    public Teacher getTaughtBy() {
        return taughtBy;
    }

    public List<Course> getCoursesTaken() {
        return coursesTaken;
    }
}
