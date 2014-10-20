package org.neo4j.ogm.mapper.domain.annotated;

import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Label;

import java.util.List;

@Label
public class User extends Account {

    @Relationship(name="Activity")
    List<UserActivity> activityList;


}
