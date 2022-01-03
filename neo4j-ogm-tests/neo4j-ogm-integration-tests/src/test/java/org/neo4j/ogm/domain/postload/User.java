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
package org.neo4j.ogm.domain.postload;

import static org.neo4j.ogm.annotation.Relationship.*;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.PostLoad;
import org.neo4j.ogm.annotation.Relationship;

/**
 * @author Frantisek Hartman
 */
public class User {

    static int postLoadCount = 0;

    Long id;

    @Relationship(type = "FRIEND_OF", direction = Direction.UNDIRECTED)
    private Set<User> friends;

    public static int getPostLoadCount() {
        return postLoadCount;
    }

    public static void resetPostLoadCount() {
        postLoadCount = 0;
    }

    public Long getId() {
        return id;
    }

    @PostLoad
    public void postLoad() {
        postLoadCount++;
    }

    public Set<User> getFriends() {
        return friends;
    }

    public void addFriend(User friend) {
        if (friends == null) {
            friends = new HashSet<>();
        }
        friends.add(friend);

        if (friend.friends == null) {
            friend.friends = new HashSet<>();
        }

        friend.friends.add(this);
    }
}
