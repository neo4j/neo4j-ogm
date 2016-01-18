/*
 * Copyright (c) 2002-2016 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with
 * separate copyright notices and license terms. Your use of the source
 * code for these subcomponents is subject to the terms and
 *  conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package org.neo4j.ogm.domain.friendships;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import java.util.Set;

/**
 * @author Vince Bickers
 */
@RelationshipEntity(type = "FRIEND_OF")
public class Friendship {

    private Long id;

    @StartNode
    private Person person;

    @EndNode
    private Person friend;

    private int strength;

    private Set<String> sharedHobbies;

    public Friendship() {
    }

    public Friendship(Person from, Person to, int strength) {
        this.person = from;
        this.friend = to;
        this.strength = strength;
    }

    public Friendship(Person person, Person friend, int strength, Set<String> sharedHobbies) {
        this.person = person;
        this.friend = friend;
        this.strength = strength;
        this.sharedHobbies = sharedHobbies;
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

    public Set<String> getSharedHobbies() {
        return sharedHobbies;
    }

    public void setSharedHobbies(Set<String> sharedHobbies) {
        this.sharedHobbies = sharedHobbies;
    }
}
