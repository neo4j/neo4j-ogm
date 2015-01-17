package org.neo4j.ogm.domain.friendships;

import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

public class Person {

    private Long id;
    private String name;

    @Relationship(type="FRIEND_OF")
    private List<Friendship> friends;

    public Person() {
        this.friends = new ArrayList<>();
    }

    public Person(String name) {
        this();
        this.name = name;
    }

    public List<Friendship> getFriends() {
        return friends;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
}
