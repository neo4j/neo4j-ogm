/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.ogm.domain.friendships;

import java.util.Set;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

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
