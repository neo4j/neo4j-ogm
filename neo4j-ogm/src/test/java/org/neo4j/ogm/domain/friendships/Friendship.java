package org.neo4j.ogm.domain.friendships;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type="FRIEND_OF")
public class Friendship {

    private Long id;

    @StartNode
    private Person person;

    @EndNode
    private Person friend;

    private int strength;

    public Friendship() {}

    public Friendship(Person from, Person to, int strength) {
        this.person = from;
        this.friend = to;
        this.strength = strength;
    }

    public Person getPerson() {
        return person;
    }

    public Person getFriend() {
        return friend;
    }

    public int getStrength() {
        return strength;
    }

    public Long getId() {
        return id;
    }
}
