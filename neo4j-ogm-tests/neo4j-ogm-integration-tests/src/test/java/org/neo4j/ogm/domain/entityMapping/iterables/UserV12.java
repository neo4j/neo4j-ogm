/*
 * Copyright (c) 2002-2019 "Neo4j,"
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
package org.neo4j.ogm.domain.entityMapping.iterables;

import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * One incoming and one outgoing relationship of the same type. Incoming iterable field and methods annotated. Outgoing methods annotated, iterable field not annotated.
 *
 * @author Luanne Misquitta
 */
public class UserV12 extends Entity {

    @Relationship(type = "LIKES")
    private Set<UserV12> friend;

    @Relationship(type = "LIKES", direction = "INCOMING")
    private Set<UserV12> friendOf;

    public UserV12() {
    }

    public Set<UserV12> getFriend() {
        return friend;
    }

    public void setFriend(Set<UserV12> friend) {
        this.friend = friend;
    }

    public Set<UserV12> getFriendOf() {
        return friendOf;
    }

    public void setFriendOf(Set<UserV12> friendOf) {
        this.friendOf = friendOf;
    }
}
