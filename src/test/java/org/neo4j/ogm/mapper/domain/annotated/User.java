package org.neo4j.ogm.mapper.domain.annotated;

import org.neo4j.ogm.annotation.Edge;
import org.neo4j.ogm.annotation.Label;

import java.util.List;

@Label
public class User extends Account {

    @Edge(name="Activity")
    List<UserActivity> activityList;


}
