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
package org.neo4j.ogm.domain.locking;

import static org.neo4j.ogm.annotation.Relationship.*;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Version;

/**
 * @author Frantisek Hartman
 */
@NodeEntity
public class User {

    private Long id;

    private String name;

    @Version
    private Long version;

    @Relationship(type = "FRIEND_OF", direction = Direction.UNDIRECTED)
    Set<FriendOf> friends = new HashSet<>();

    public User() {
    }

    public User(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override public String toString() {
        return "User{" +
            "id=" + id +
            ", version=" + version +
            '}';
    }

    public FriendOf addFriend(User user) {
        FriendOf friendOf = new FriendOf(this, user);
        friends.add(friendOf);
        user.friends.add(friendOf);
        return friendOf;
    }

    public void addFriend(FriendOf friendOf) {
        friends.add(friendOf);
        friendOf.setTo(this);
    }

    public void clearFriends() {
        friends.clear();
    }
}
