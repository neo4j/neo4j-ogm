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
package org.neo4j.ogm.domain.entityMapping.iterables;

import java.util.Set;

import org.neo4j.ogm.annotation.Relationship;

/**
 * One outgoing and one incoming relationship of the same type. Incoming iterable field and methods annotated. Outgoing iterable field annotated , methods not annotated.
 *
 * @author Luanne Misquitta
 */
public class UserV11 extends Entity {

    @Relationship(type = "LIKES", direction = Relationship.Direction.OUTGOING)
    private Set<UserV11> friend;

    @Relationship(type = "LIKES", direction = Relationship.Direction.INCOMING)
    private Set<UserV11> friendOf;

    public UserV11() {
    }

    public Set<UserV11> getFriend() {
        return friend;
    }

    public void setFriend(Set<UserV11> friend) {
        this.friend = friend;
    }

    public Set<UserV11> getFriendOf() {
        return friendOf;
    }

    public void setFriendOf(Set<UserV11> friendOf) {
        this.friendOf = friendOf;
    }
}
