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
package org.neo4j.ogm.domain.social;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.ogm.annotation.Relationship;

/**
 * POJO used to test the direction of an undirected relationship.
 *
 * @author Luanne Misquitta
 * @author Michael J. Simons
 */
public class User {

    private Long id;
    private String name;

    @Relationship(type = "FRIEND", direction = Relationship.Direction.UNDIRECTED)
    private List<User> friends = new ArrayList<>();

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public List<User> getFriends() {
        return friends;
    }

    public void setFriends(List<User> friends) {
        this.friends = friends;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void befriend(User user) {
        friends.add(user);
        user.friends.add(this);
    }

    public void unfriend(User user) {
        friends.remove(user);
        user.friends.remove(this);
    }

    @Override public String toString() {
        return "User{" +
            "name='" + name + '\'' +
            '}';
    }
}
