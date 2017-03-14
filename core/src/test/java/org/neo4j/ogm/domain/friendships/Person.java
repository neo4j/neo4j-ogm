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

import org.neo4j.ogm.annotation.Relationship;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vince Bickers
 */
public class Person {

    private Long id;
    private String name;

    @Relationship(type = "FRIEND_OF")
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

    public Friendship addFriend(Person newFriend) {
        Friendship friendship = new Friendship(this, newFriend, 5);
        this.friends.add(friendship);
        return friendship;
    }

}
